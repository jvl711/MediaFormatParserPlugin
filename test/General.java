/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import jvl.FFmpeg.jni.*;

/**
 *
 * @author jvl711
 */
public class General {
    
    public General() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() 
    {
        
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void TestLibraryConfig()
    {
        System.out.println(AVGlobal.getAVError(-12));
    }
}
