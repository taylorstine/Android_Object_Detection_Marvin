package com.tstine.marvinas;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera;
import android.provider.MediaStore.Files.FileColumns;
import java.io.*;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;
import android.os.Environment;
import android.content.Context;
import android.app.DialogFragment;

public class PictureTaker implements PictureCallback{
	private Context mCtx;
	public PictureTaker( Context ctx ){ mCtx = ctx; }
	
	public void onPictureTaken( byte[] data, Camera camera ){
		File pictureFile = getOutputMediaFile( FileColumns.MEDIA_TYPE_IMAGE );
		if(pictureFile== null) 
			throw new RuntimeException("Error creating media file,check storage permissions " );
		try{
			FileOutputStream fos = new FileOutputStream( pictureFile );
			fos.write( data );
			fos.close();
			MediaScannerConnection
				.scanFile( mCtx,
									 new String[] {pictureFile.toString()},
									 null,
									 new MediaScannerConnection.OnScanCompletedListener(){
										 public void onScanCompleted( String path, Uri uri ){
											 Log.i("ExternalStorage", "Scanned " + path + ":");
											 Log.i("ExternalStorage", "-> uri= " + uri );
										 }
									 });
		}catch ( FileNotFoundException e ){
			throw new RuntimeException("File not found " + e.getMessage() );
		} catch( IOException e ){
			throw new RuntimeException("Error accessing file " + e.getMessage() );
		}
		camera.startPreview();
		DialogFragment frag = new UserInputDialog(pictureFile);
		frag.show( ((CameraActivity)mCtx).getFragmentManager(), "dialog" );

		//change this
		// Intent intent = new Intent( null, Uri.fromFile( pictureFile ),
		// CameraActivity.this, DetectionService.class );
		// startService(intent);
	}


	private static Uri getOutputMediaFileUri( int type ){
		return Uri.fromFile( getOutputMediaFile(  type ) );
	}
	private static File getOutputMediaFile( int type ){
		File mediaStorageDir =
			new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES ), "Marvin" );
		if( !mediaStorageDir.exists() ){
			if( !mediaStorageDir.mkdirs() ){
				throw new RuntimeException("Failed to create directory");
			}
		}
		
		String timeStamp = Installation.getTimestamp();
		File mediaFile;
		if( type == FileColumns.MEDIA_TYPE_IMAGE ){
			mediaFile = new File( mediaStorageDir.getPath() + File.separator + 
														"IMG_"+ timeStamp + ".jpg" );
		}else if( type == FileColumns.MEDIA_TYPE_VIDEO ){
			mediaFile = new File( mediaStorageDir.getPath() + File.separator +
														"VID_" + timeStamp + ".mp4" );
		}
		else{
			throw new RuntimeException("Invalid save format requested" );
		}
		return mediaFile;
	}

}
