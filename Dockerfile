# First stage: Build OpenSSL
FROM ubuntu:24.04 as build-base

ARG BUILDKIT_SBOM_SCAN_STAGE=true

RUN set -eux; \
	apt-get update; \
	apt-get install -y --no-install-recommends \
		build-essential \
		ca-certificates \
		gnupg \
		libncurses5-dev \
		wget

FROM build-base as openssl-builder

ARG BUILDKIT_SBOM_SCAN_STAGE=true

# Default to a PGP keyserver that pgp-happy-eyeballs recognizes, but allow for substitutions locally
ARG PGP_KEYSERVER=keyserver.ubuntu.com
ENV OPENSSL_VERSION 3.3.2
ENV OPENSSL_SOURCE_SHA256="2e8a40b01979afe8be0bbfb3de5dc1c6709fedb46d6c89c10da114ab5fc3d281"
ENV OPENSSL_PGP_KEY_IDS="0xBA5473A2B0587B07FB27CF2D216094DFD0CB81EF"
ENV OTP_VERSION 26.2.5.3
ENV OTP_SOURCE_SHA256="c2707ce08e91235145cdfc487352f05570a2a0bddf1c478154549eb9e68805b0"
ENV ERLANG_INSTALL_PATH_PREFIX /opt/erlang
ENV OPENSSL_INSTALL_PATH_PREFIX /opt/openssl

# Install dependencies required to build Erlang/OTP from source
RUN set -eux; \
	OPENSSL_SOURCE_URL="https://github.com/openssl/openssl/releases/download/openssl-$OPENSSL_VERSION/openssl-$OPENSSL_VERSION.tar.gz"; \
	OPENSSL_PATH="/usr/local/src/openssl-$OPENSSL_VERSION"; \
	OPENSSL_CONFIG_DIR="$OPENSSL_INSTALL_PATH_PREFIX/etc/ssl"; \
	# Install OpenSSL dependencies
	wget --progress dot:giga --output-document "$OPENSSL_PATH.tar.gz.asc" "$OPENSSL_SOURCE_URL.asc"; \
	wget --progress dot:giga --output-document "$OPENSSL_PATH.tar.gz" "$OPENSSL_SOURCE_URL"; \
	export GNUPGHOME="$(mktemp -d)"; \
	for key in $OPENSSL_PGP_KEY_IDS; do \
		gpg --batch --keyserver "$PGP_KEYSERVER" --recv-keys "$key"; \
	done; \
	gpg --batch --verify "$OPENSSL_PATH.tar.gz.asc" "$OPENSSL_PATH.tar.gz"; \
	gpgconf --kill all; \
	rm -rf "$GNUPGHOME"; \
	echo "$OPENSSL_SOURCE_SHA256 *$OPENSSL_PATH.tar.gz" | sha256sum --check --strict -; \
	mkdir -p "$OPENSSL_PATH"; \
	tar --extract --file "$OPENSSL_PATH.tar.gz" --directory "$OPENSSL_PATH" --strip-components 1; \
	# Configure OpenSSL for compilation
	cd "$OPENSSL_PATH"; \
	dpkgArch="$(dpkg --print-architecture)"; dpkgArch="${dpkgArch##*-}"; \
	case "$dpkgArch" in \
		amd64) opensslMachine='linux-x86_64' ;; \
		arm64) opensslMachine='linux-aarch64' ;; \
		armhf) opensslMachine='linux-armv4'; opensslExtraConfig='-march=armv7-a+fp' ;; \
		i386) opensslMachine='linux-x86' ;; \
		ppc64el) opensslMachine='linux-ppc64le' ;; \
		riscv64) opensslMachine='linux64-riscv64' ;; \
		s390x) opensslMachine='linux64-s390x' ;; \
		*) echo >&2 "error: unsupported arch: '$apkArch'"; exit 1 ;; \
	esac; \
	MACHINE="$opensslMachine" ./Configure \
		"$opensslMachine" \
		enable-fips \
		--prefix="$OPENSSL_INSTALL_PATH_PREFIX" \
		--openssldir="$OPENSSL_CONFIG_DIR" \
		--libdir="$OPENSSL_INSTALL_PATH_PREFIX/lib" \
		-Wl,-rpath="$OPENSSL_INSTALL_PATH_PREFIX/lib"; \
	make -j "$(getconf _NPROCESSORS_ONLN)"; \
	make install_sw install_ssldirs install_fips; \
	ldconfig; \
	# use Debian's CA certificates
	rmdir "$OPENSSL_CONFIG_DIR/certs" "$OPENSSL_CONFIG_DIR/private"; \
	ln -sf /etc/ssl/certs /etc/ssl/private "$OPENSSL_CONFIG_DIR"

# Smoke test OpenSSL
RUN $OPENSSL_INSTALL_PATH_PREFIX/bin/openssl version

# Second stage: Build Erlang/OTP
FROM openssl-builder as erlang-builder

RUN set -eux; \
	OTP_SOURCE_URL="https://github.com/erlang/otp/releases/download/OTP-$OTP_VERSION/otp_src_$OTP_VERSION.tar.gz"; \
	OTP_PATH="/usr/local/src/otp-$OTP_VERSION"; \
	mkdir -p "$OTP_PATH"; \
	wget --progress dot:giga --output-document "$OTP_PATH.tar.gz" "$OTP_SOURCE_URL"; \
	echo "$OTP_SOURCE_SHA256 *$OTP_PATH.tar.gz" | sha256sum --check --strict -; \
	tar --extract --file "$OTP_PATH.tar.gz" --directory "$OTP_PATH" --strip-components 1; \
	# Apply patch for time64 compilation issues
	wget --output-document otp-time64.patch 'https://github.com/erlang/otp/pull/7952.patch?full_index=1'; \
	patch --input="$PWD/otp-time64.patch" --directory="$OTP_PATH" --strip=1; \
	rm otp-time64.patch; \
	# Configure Erlang/OTP for compilation
	cd "$OTP_PATH"; \
	export ERL_TOP="$OTP_PATH"; \
	CFLAGS="$(dpkg-buildflags --get CFLAGS)"; export CFLAGS; \
	export CFLAGS="$CFLAGS -Wl,-rpath=$OPENSSL_INSTALL_PATH_PREFIX/lib"; \
	hostArch="$(dpkg-architecture --query DEB_HOST_GNU_TYPE)"; \
	buildArch="$(dpkg-architecture --query DEB_BUILD_GNU_TYPE)"; \
	dpkgArch="$(dpkg --print-architecture)"; dpkgArch="${dpkgArch##*-}"; \
	jitFlag=; \
	case "$dpkgArch" in \
		amd64 | arm64) jitFlag='--enable-jit' ;; \
	esac; \
	./configure \
		--prefix="$ERLANG_INSTALL_PATH_PREFIX" \
		--host="$hostArch" \
		--build="$buildArch" \
		--disable-hipe \
		--disable-sctp \
		--enable-builtin-zlib \
		--enable-clock-gettime \
		--enable-hybrid-heap \
		--enable-kernel-poll \
		--enable-smp-support \
		--enable-threads \
		--with-ssl="$OPENSSL_INSTALL_PATH_PREFIX" \
		--without-common_test \
		--without-debugger \
		--without-dialyzer \
		--without-eunit \
		$jitFlag; \
	make -j "$(getconf _NPROCESSORS_ONLN)" GEN_OPT_FLGS="-O2 -fno-strict-aliasing"; \
	make install; \
	# Remove unnecessary files
	find "$ERLANG_INSTALL_PATH_PREFIX/lib/erlang" -type d -name examples -exec rm -rf '{}' +; \
	find "$ERLANG_INSTALL_PATH_PREFIX/lib/erlang" -type d -name src -exec rm -rf '{}' +; \
	find "$ERLANG_INSTALL_PATH_PREFIX/lib/erlang" -type d -name include -exec rm -rf '{}' +

# Final stage: Set up application environment
FROM ubuntu:24.04

ENV ERLANG_INSTALL_PATH_PREFIX /opt/erlang
ENV OPENSSL_INSTALL_PATH_PREFIX /opt/openssl
COPY --from=erlang-builder $ERLANG_INSTALL_PATH_PREFIX $ERLANG_INSTALL_PATH_PREFIX
COPY --from=openssl-builder $OPENSSL_INSTALL_PATH_PREFIX $OPENSSL_INSTALL_PATH_PREFIX

ENV PATH $ERLANG_INSTALL_PATH_PREFIX/bin:$OPENSSL_INSTALL_PATH_PREFIX/bin:$PATH

RUN set -eux; \
	# Configure OpenSSL to use system certs
	ln -vsf /etc/ssl/certs /etc/ssl/private "$OPENSSL_INSTALL_PATH_PREFIX/etc/ssl"; \
	ldconfig; \
	sed -i.ORIG -e "/\.include.*fips/ s!.*!.include $OPENSSL_INSTALL_PATH_PREFIX/etc/ssl/fipsmodule.cnf!" \
		-e '/# fips =/s/.*/fips = fips_sect/' "$OPENSSL_INSTALL_PATH_PREFIX/etc/ssl/openssl.cnf"; \
	sed -i.ORIG -e '/^activate/s/^/#/' "$OPENSSL_INSTALL_PATH_PREFIX/etc/ssl/fipsmodule.cnf";
