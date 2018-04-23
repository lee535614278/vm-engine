package com.vm;

import com.vm.admin.listener.ApplicationRefreshedListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * Created by ZhangKe on 2018/2/3.
 */


@SpringBootApplication
@ServletComponentScan
@EnableDiscoveryClient
//@RemoteApplicationEventScan(basePackages = "com.example.configclient.event")
public class ProviderAdminApplication {

    public static void main(String[] args) {

        SpringApplication springApplication = new SpringApplication(ProviderAdminApplication.class);
        springApplication.addListeners(new ApplicationRefreshedListener());
        springApplication.run(args);

    }

}
