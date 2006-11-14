package org.newdawn.slick.opengl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.glu.GLU;
import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.ResourceLoader;

/**
 * A texture loaded based on many old versions that will load image data from a file
 * and produce OpenGL textures.
 * 
 * @see ImageData
 * 
 * @author kevin
 */
public class TextureLoader {
	/** The standard texture loaded used everywhere */
	private static final TextureLoader loader = new TextureLoader();
	
	/**
	 * Get the single instance of this texture loader
	 * 
	 * @return The single instance of the texture loader
	 */
	public static TextureLoader get() {
		return loader;
	}
	
    /** The table of textures that have been loaded in this loader */
    private HashMap texturesLinear = new HashMap();
    /** The table of textures that have been loaded in this loader */
    private HashMap texturesNearest = new HashMap();
    /** The destination pixel format */
    private int dstPixelFormat = GL11.GL_RGBA;
    
    /** 
     * Create a new texture loader based on the game panel
     */
    public TextureLoader() {
    }
    
    /**
     * Tell the loader to produce 16 bit textures
     */
    public void set16BitMode() {
    	dstPixelFormat = GL11.GL_RGBA16;
    }
    
    /**
     * Create a new texture ID 
     *
     * @return A new texture ID
     */
    private int createTextureID() 
    { 
       IntBuffer tmp = createIntBuffer(1); 
       GL11.glGenTextures(tmp); 
       return tmp.get(0);
    } 

    /**
     * Get a texture from a specific file
     * 
     * @param source The file to load the texture from
     * @param flipped True if we should flip the texture on the y axis while loading
     * @param filter The filter to use
     * @return The texture loaded
     * @throws IOException Indicates a failure to load the image
     */
    public Texture getTexture(File source, boolean flipped,int filter) throws IOException {
    	String resourceName = source.getAbsolutePath();
    	InputStream in = new FileInputStream(source);
    	
    	return getTexture(in, resourceName, flipped, filter);
    }

    /**
     * Get a texture from a resource location
     * 
     * @param resourceName The location to load the texture from
     * @param flipped True if we should flip the texture on the y axis while loading
     * @param filter The filter to use when scaling the texture
     * @return The texture loaded
     * @throws IOException Indicates a failure to load the image
     */
    public Texture getTexture(String resourceName, boolean flipped, int filter) throws IOException {
    	InputStream in = ResourceLoader.getResourceAsStream(resourceName);
    	
    	return getTexture(in, resourceName, flipped, filter);
    }
    
    /**
     * Get a texture from a image file
     * 
     * @param in The stream from which we can load the image
     * @param resourceName The name to give this image in the internal cache
     * @param flipped True if we should flip the image on the y-axis while loading
     * @param filter The filter to use when scaling the texture
     * @return The texture loaded
     * @throws IOException Indicates a failure to load the image
     */
    public Texture getTexture(InputStream in, String resourceName, boolean flipped, int filter) throws IOException {
        HashMap hash = texturesLinear;
        if (filter == GL11.GL_NEAREST) {
        	hash = texturesNearest;
        }
        
    	Texture tex = (Texture) hash.get(resourceName);
        if (tex != null) {
        	return tex;
        }
        
        tex = getTexture(in, resourceName,
                         GL11.GL_TEXTURE_2D, 
                         filter, 
                         filter, flipped);
        
        hash.put(resourceName,tex);
        
        return tex;
    }

    /**
     * Get a texture from a image file
     * 
     * @param in The stream from which we can load the image
     * @param resourceName The name to give this image in the internal cache
     * @param flipped True if we should flip the image on the y-axis while loading
     * @param target The texture target we're loading this texture into
     * @param minFilter The scaling down filter
     * @param magFilter The scaling up filter
     * @return The texture loaded
     * @throws IOException Indicates a failure to load the image
     */
    public Texture getTexture(InputStream in, 
    						  String resourceName, 
                              int target, 
                              int magFilter, 
                              int minFilter, boolean flipped) throws IOException 
    { 
        // create the texture ID for this texture 
        int textureID = createTextureID(); 
        Texture texture = new Texture(resourceName, target, textureID); 
        
        // bind this texture 
        GL11.glBindTexture(target, textureID); 
 
        ByteBuffer textureBuffer;
        int width;
        int height;
        int texWidth;
        int texHeight;
        
        boolean hasAlpha;
        
        ImageData imageData = null;
        if (resourceName.endsWith(".tga")) {
        	imageData = new TGAImageData();
        } else {
        	imageData = new ImageIOImageData();
        }
        
    	textureBuffer = imageData.loadImage(new BufferedInputStream(in), flipped);
    	
    	width = imageData.getWidth();
    	height = imageData.getHeight();
    	hasAlpha = imageData.getDepth() == 32;
    	
    	texture.setTextureWidth(imageData.getTexWidth());
    	texture.setTextureHeight(imageData.getTexHeight());

        texWidth = texture.getTextureWidth();
        texHeight = texture.getTextureHeight();
        
        int srcPixelFormat = hasAlpha ? GL11.GL_RGBA : GL11.GL_RGB;
        int componentCount = hasAlpha ? 4 : 3;
        
        texture.setWidth(width);
        texture.setHeight(height);
        
        GL11.glTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER, minFilter); 
        GL11.glTexParameteri(target, GL11.GL_TEXTURE_MAG_FILTER, magFilter); 
        
        if (minFilter == GL11.GL_LINEAR_MIPMAP_NEAREST) {
        	// generate a mip map textur
        	GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, componentCount, texWidth,
        					      texHeight, srcPixelFormat, 
        					      GL11.GL_UNSIGNED_BYTE, textureBuffer);
        } else {
	        // produce a texture from the byte buffer
	        GL11.glTexImage2D(target, 
	                      0, 
	                      dstPixelFormat, 
	                      get2Fold(width), 
	                      get2Fold(height), 
	                      0, 
	                      srcPixelFormat, 
	                      GL11.GL_UNSIGNED_BYTE, 
	                      textureBuffer); 
        }
        
        return texture; 
    } 
    
    /**
     * Get a texture from a image file
     * 
     * @param dataSource The image data to generate the texture from
     * @param filter The filter to use when scaling the texture
     * @return The texture created
     */
    public Texture getTexture(ImageData dataSource, int filter)
    { 
    	int target = GL11.GL_TEXTURE_2D;
    	
        // create the texture ID for this texture 
        int textureID = createTextureID(); 
        Texture texture = new Texture("generated:"+dataSource, target ,textureID); 
        
        int minFilter = filter;
        int magFilter = filter;
        boolean flipped = false;
        
        // bind this texture 
        GL11.glBindTexture(target, textureID); 
 
        ByteBuffer textureBuffer;
        int width;
        int height;
        int texWidth;
        int texHeight;
        
        boolean hasAlpha;
    	textureBuffer = dataSource.getImageBufferData();
    	
    	width = dataSource.getWidth();
    	height = dataSource.getHeight();
    	hasAlpha = dataSource.getDepth() == 32;
    	
    	texture.setTextureWidth(dataSource.getTexWidth());
    	texture.setTextureHeight(dataSource.getTexHeight());

        texWidth = texture.getTextureWidth();
        texHeight = texture.getTextureHeight();
        
        int srcPixelFormat = hasAlpha ? GL11.GL_RGBA : GL11.GL_RGB;
        int componentCount = hasAlpha ? 4 : 3;
        
        texture.setWidth(width);
        texture.setHeight(height);
        
        GL11.glTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER, minFilter); 
        GL11.glTexParameteri(target, GL11.GL_TEXTURE_MAG_FILTER, magFilter); 
        
        if (minFilter == GL11.GL_LINEAR_MIPMAP_NEAREST) {
        	// generate a mip map textur
        	GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, componentCount, texWidth,
        					      texHeight, srcPixelFormat, 
        					      GL11.GL_UNSIGNED_BYTE, textureBuffer);
        } else {
	        // produce a texture from the byte buffer
	        GL11.glTexImage2D(target, 
	                      0, 
	                      dstPixelFormat, 
	                      get2Fold(width), 
	                      get2Fold(height), 
	                      0, 
	                      srcPixelFormat, 
	                      GL11.GL_UNSIGNED_BYTE, 
	                      textureBuffer); 
        }
        
        return texture; 
    } 
    
    /**
     * Get the closest greater power of 2 to the fold number
     * 
     * @param fold The target number
     * @return The power of 2
     */
    private int get2Fold(int fold) {
        int ret = 2;
        while (ret < fold) {
            ret *= 2;
        }
        return ret;
    } 
    
    /**
     * Creates an integer buffer to hold specified ints
     * - strictly a utility method
     *
     * @param size how many int to contain
     * @return created IntBuffer
     */
    protected IntBuffer createIntBuffer(int size) {
      ByteBuffer temp = ByteBuffer.allocateDirect(4 * size);
      temp.order(ByteOrder.nativeOrder());

      return temp.asIntBuffer();
    }    
}