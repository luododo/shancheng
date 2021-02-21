package com.atguigu.gulimall.thirdpart;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class GilimallThirdPartApplicationTests {
	@Autowired
	OSSClient ossClient;
	@Test
	void testUpload()throws FileNotFoundException {
		InputStream inputStream = new FileInputStream("D:\\picture\\0f2442a7d933c895202a55eed31373f0830200cb.jpg");
		ossClient.putObject("gulimall-adverseq","test04.jpg",inputStream);
	}

}
