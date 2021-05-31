package com.atguigu.gmall.wms.fegin;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("wms-service")
public interface GmallWmsFeginClient extends GmallWmsApi {
}
