package org.springframework.samples.petclinic.system;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebConfigurationTests {

	private final WebConfiguration configuration = new WebConfiguration();

	@Test
	void localeResolverUsesEnglishAsDefaultLocale() {
		LocaleResolver localeResolver = this.configuration.localeResolver();

		assertThat(localeResolver).isInstanceOf(SessionLocaleResolver.class);
		assertThat(localeResolver.resolveLocale(new MockHttpServletRequest())).isEqualTo(Locale.ENGLISH);
	}

	@Test
	void localeChangeInterceptorUsesLangParameter() {
		LocaleChangeInterceptor interceptor = this.configuration.localeChangeInterceptor();

		assertThat(interceptor.getParamName()).isEqualTo("lang");
	}

	@Test
	void addInterceptorsRegistersLocaleChangeInterceptor() {
		InterceptorRegistry registry = mock(InterceptorRegistry.class);
		this.configuration.addInterceptors(registry);

		verify(registry).addInterceptor(any(LocaleChangeInterceptor.class));
	}

}
