package com.yetu.manta.client;

import java.util.ArrayList;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.mime.TypedFile;

import com.yetu.manta.client.representations.MantaObject;

public interface MantaClientInterface {

	@PUT("/{user}/{path}")
	@Headers("content-type: application/json; type=directory")
	public void createDirectory(@Path("user") String user,
			@Path(value = "path", encode = false) String path);

	@DELETE("/{user}/{path}")
	public void deleteDirectory(@Path("user") String user,
			@Path(value = "path", encode = false) String path);

	@PUT("/{user}/{path}")
	@Headers("content-type: application/octet-stream")
	public void putFile(@Path("user") String user,
			@Path(value = "path", encode = false) String path,
			@Body TypedFile binaryObject);

	@GET("/{user}/{path}")
	public ArrayList<MantaObject> list(@Path("user") String user,
			@Path(value = "path", encode = false) String path);

	@GET("/{user}/{path}")
	public Response getRawObject(@Path("user") String user,
			@Path(value = "path", encode = false) String path);
	
	@GET("/{user}/{path}")
	public Response getObjectMetadata(@Path("user") String user,
			@Path(value = "path", encode = false) String path);

	@DELETE("/{user}/{path}")
	public void deleteObject(@Path("user") String user,
			@Path(value = "path", encode = false) String path);
}
