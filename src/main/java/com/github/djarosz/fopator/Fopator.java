package com.github.djarosz.fopator;

import com.github.djarosz.fopator.config.Config;
import com.github.djarosz.fopator.config.RegexFileMapperConfig;
import com.github.djarosz.fopator.pdf.PdfCreator;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.RegexPatternFileListFilter;
import org.springframework.integration.file.locking.NioFileLocker;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.support.PeriodicTrigger;

@SpringBootApplication
@EnableConfigurationProperties({Config.class, RegexFileMapperConfig.class})
public class Fopator {

	@Autowired
	private Config config;

	public static void main(String[] args) {
		SpringApplication.run(Fopator.class, args);
	}

	@Bean
	@ServiceActivator(inputChannel = "pdfCreatorInputChannel")
	public PdfCreator pdfCreator() {
		return new PdfCreator();
	}

	@Bean
	@InboundChannelAdapter(value = "pdfCreatorInputChannel", poller = @Poller("xmlFilePoller"))
	public MessageSource<File> xmlSource() {
		FileReadingMessageSource source = new FileReadingMessageSource();
		source.setDirectory(config.getXmlDir());
		source.setAutoCreateDirectory(true);
		source.setFilter(new CompositeFileListFilter<>(
				Arrays.asList(
						new AgeFileListFilter(config.getXmlFileAge()),
						new AcceptOnceFileListFilter<File>(config.getXmlSeenFilesQueueSize()),
						new RegexPatternFileListFilter(config.getXmlFilePattern())
				)));
		return source;
	}

	@Bean
	public PollerMetadata xmlFilePoller() {
		PollerMetadata poller = new PollerMetadata();

		poller.setMaxMessagesPerPoll(config.getXmlFilesPerPoll());
		PeriodicTrigger trigger = new PeriodicTrigger(config.getXmlFilesPollInterval());
		trigger.setFixedRate(false);
		poller.setTrigger(trigger);

		return poller;
	}

	@Bean
	public MessageChannel pdfCreatorInputChannel() {
		return new ExecutorChannel(pdfCreatorExecutor());
	}

	@Bean
	public Executor pdfCreatorExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setMaxPoolSize(config.getPdfGeneratorThreads());
		executor.setQueueCapacity(config.getPdfGeneratorQueueSize());
		return executor;
	}

	@Bean
	public NioFileLocker fileLocker() {
		return new NioFileLocker();
	}

}
