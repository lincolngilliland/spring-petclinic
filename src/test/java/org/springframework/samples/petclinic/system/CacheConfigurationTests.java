package org.springframework.samples.petclinic.system;

import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

import org.junit.jupiter.api.Test;
import org.springframework.boot.cache.autoconfigure.JCacheManagerCustomizer;

import java.net.URI;
import java.util.Collections;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigurationTests {

	private final CacheConfiguration configuration = new CacheConfiguration();

	@Test
	void customizerCreatesVetsCacheWithStatisticsEnabled() {
		RecordingCacheManager cacheManager = new RecordingCacheManager();
		JCacheManagerCustomizer customizer = this.configuration.petclinicCacheConfigurationCustomizer();

		customizer.customize(cacheManager);

		assertThat(cacheManager.cacheName).isEqualTo("vets");
		assertThat(cacheManager.configuration).isInstanceOf(MutableConfiguration.class);
		MutableConfiguration<?, ?> mutableConfiguration = (MutableConfiguration<?, ?>) cacheManager.configuration;
		assertThat(mutableConfiguration.isStatisticsEnabled()).isTrue();
	}

	private static final class RecordingCacheManager implements CacheManager {

		private String cacheName;

		private Configuration<?, ?> configuration;

		@Override
		public <K, V, C extends Configuration<K, V>> javax.cache.Cache<K, V> createCache(String cacheName,
				C configuration) throws IllegalArgumentException {
			this.cacheName = cacheName;
			this.configuration = configuration;
			return null;
		}

		@Override
		public CachingProvider getCachingProvider() {
			return null;
		}

		@Override
		public URI getURI() {
			return URI.create("in-memory:recording");
		}

		@Override
		public ClassLoader getClassLoader() {
			return getClass().getClassLoader();
		}

		@Override
		public Properties getProperties() {
			return new Properties();
		}

		@Override
		public <K, V> javax.cache.Cache<K, V> getCache(String cacheName, Class<K> keyType, Class<V> valueType) {
			return null;
		}

		@Override
		public <K, V> javax.cache.Cache<K, V> getCache(String cacheName) {
			return null;
		}

		@Override
		public Iterable<String> getCacheNames() {
			return Collections.emptyList();
		}

		@Override
		public void destroyCache(String cacheName) {
		}

		@Override
		public void enableManagement(String cacheName, boolean enabled) {
		}

		@Override
		public void enableStatistics(String cacheName, boolean enabled) {
		}

		@Override
		public void close() {
		}

		@Override
		public boolean isClosed() {
			return false;
		}

		@Override
		public <T> T unwrap(Class<T> clazz) {
			throw new CacheException("Not a wrapper");
		}

	}

}
