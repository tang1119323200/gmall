package com.atguigu.gmall.pms.fegin;



import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-service")
public interface GmallSmsFeginClient extends GmallSmsApi {

}
