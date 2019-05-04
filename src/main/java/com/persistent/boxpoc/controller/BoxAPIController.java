package com.persistent.boxpoc.controller;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxUser;
import com.box.sdk.CreateUserParams;
import com.box.sdk.IAccessTokenCache;
import com.box.sdk.InMemoryLRUAccessTokenCache;
import com.persistent.boxpoc.GenericResponse;


@RestController
@RequestMapping("/boxapipoc")
public class BoxAPIController {

	private static final String CONFIG_FILE = "D:\\BoxAPIDocument\\195607646_r8auulo6_config.json";
	private static final int MAX_CACHE_ENTRIES = 100;
	//public static final URLTemplate UPLOAD_SESSION_URL_TEMPLATE = new URLTemplate("files/%s/upload_sessions");
	
	
	@PostMapping(value = "/createAppUser")
	public GenericResponse<?> createAppUser(@RequestParam("appUserName") String appUserName, @RequestParam("extAppUserId") String extAppUserId) {

		IAccessTokenCache accessTokenCache = new InMemoryLRUAccessTokenCache(MAX_CACHE_ENTRIES);

		try {
			Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
			field.setAccessible(true);
			field.set(null, java.lang.Boolean.FALSE);

			Reader reader = new FileReader(CONFIG_FILE);
			BoxConfig boxConfig = BoxConfig.readFrom(reader);

			BoxDeveloperEditionAPIConnection api = BoxDeveloperEditionAPIConnection
					.getAppEnterpriseConnection(boxConfig, accessTokenCache);

			CreateUserParams params = new CreateUserParams();
			params.setSpaceAmount(1073741824); // 1 GB
			params.setExternalAppUserId(extAppUserId);
			BoxUser.Info user = BoxUser.createAppUser(api, appUserName, params);
			System.out.format("App User created with name %s and id %s\n\n", appUserName, user.getID());

		} catch (NoSuchFieldException | SecurityException | ClassNotFoundException | IllegalArgumentException
				| IllegalAccessException | IOException e) {
			return new GenericResponse<Object>(
					"You failed to create app user :: " + appUserName + " . Cause of the error is :: " + e.getMessage(),
					GenericResponse.FAILURE, HttpStatus.NOT_FOUND.value(), null);
		}
		return new GenericResponse<Object>("App user created with name :: " + appUserName, GenericResponse.SUCCESS,
				HttpStatus.OK.value(), null);
	}

	@PostMapping(value = "/createHomeDirectory")
	public GenericResponse<?> createHomeDirectory(@RequestParam("homeDirName") String homeDirName) {

		IAccessTokenCache accessTokenCache = new InMemoryLRUAccessTokenCache(MAX_CACHE_ENTRIES);
		try {
			Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
			field.setAccessible(true);
			field.set(null, java.lang.Boolean.FALSE);
			Reader reader = new FileReader(CONFIG_FILE);
			BoxConfig boxConfig = BoxConfig.readFrom(reader);
			BoxDeveloperEditionAPIConnection api = BoxDeveloperEditionAPIConnection
					.getAppEnterpriseConnection(boxConfig, accessTokenCache);

			String parentFolderID = "0";
			BoxFolder parentFolder = new BoxFolder(api, parentFolderID);
			BoxFolder.Info childFolderInfo = parentFolder.createFolder(homeDirName);

			System.out.println("Directory created at ->" + childFolderInfo.getCreatedAt() + " and name of folder is ->"
					+ childFolderInfo.getName());

		} catch (NoSuchFieldException | SecurityException | ClassNotFoundException | IllegalArgumentException
				| IllegalAccessException | IOException e) {
			return new GenericResponse<Object>("You failed to create home directory :: " + homeDirName
					+ " . Cause of the error is :: " + e.getMessage(), GenericResponse.FAILURE,
					HttpStatus.NOT_FOUND.value(), null);
		}
		return new GenericResponse<Object>("Home directory created with name :: " + homeDirName,
				GenericResponse.SUCCESS, HttpStatus.OK.value(), null);
	}
	
 }
