import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import javax.imageio.ImageIO

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement

import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.webui.driver.DriverFactory
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

import internal.GlobalVariable as GlobalVariable

def url = "https://www.youtube.com/watch?v=WndOChZSjTk"
def title = "Lets_Kinniku_Together"
def waitSeconds = 11
def criteriaPercent = 90.0

WebUI.openBrowser('')
WebUI.setViewPortSize(1024, 768)
WebDriver driver = DriverFactory.getWebDriver()

// start loading the YouTube page
WebUI.navigateToUrl(url)  // video will be started on load

// wait for the YouTube page to load
WebUI.verifyElementPresent(
	findTestObject('Page - YouTube/button_ytp-play-button'),
	10, FailureHandling.STOP_ON_FAILURE)

// <video> element in the YouTube page
WebElement mainVideo = driver.findElement(By.cssSelector("video.html5-main-video"))

// <button> element to stop/play video in the YouTube page
WebElement playButton = driver.findElement(By.cssSelector("button.ytp-play-button"))

// verify if the YouTube Vido is autoplaying or not
Map<String, Object> result =
	CustomKeywords.'com.kazurayam.ksbackyard.ScreenshotDriver.verifyVideoInMotion'(
		driver, mainVideo, playButton,
		waitSeconds, criteriaPercent)
	/* result is a Map object
	 *         [ 'evaluated': Boolean,
	 *           'diffratio': Number,
	 *           'criteria' : Number,
	 *           'image1'   : BufferedImage,
	 *           'image2'   : BufferedImage,
	 *           'imagediff': BufferedImage
	 *         ]
	 */
	
// create tmp dir where we stort the PNG files
Path tmpDir = Paths.get(RunConfiguration.getProjectDir()).resolve('tmp/video')
Files.createDirectories(tmpDir)

// write the screenshot taken at the start
Path png1 = tmpDir.resolve("${title}_1st.png")
ImageIO.write(result.get('image1'), "PNG", png1.toFile())

// write the screenshot taken after waitSeconds
Path png2 = tmpDir.resolve("${title}_2nd.png")
ImageIO.write(result.get('image2'), "PNG", png2.toFile())

// write the imageDiff between the above 2 screenshots
String descriptor = "(${result.get('diffratiostr')})${result.get('evaluated')?'':'FAILED'}"
Path pngDiff = tmpDir.resolve("${title}_diff${descriptor}.png")
ImageIO.write(result.get('imagediff'), "PNG", pngDiff.toFile())

WebUI.closeBrowser()

println "['url':${url}, 'title':'${title}','result':['evaluated':${result.evaluated}, 'diffratio':${result.diffratio}," +
	" 'criteria':${result.criteria}]]"

// if the movie autoplaying or not
Boolean isInMotion = (Boolean)result.get('evaluated')

// pass when the Video is in motion, otherwise fail, output to the log
CustomKeywords.'com.kazurayam.ksbackyard.Assert.assertTrue'(
	"movie ${title} at ${url} is not autoplaying",
	isInMotion,
	FailureHandling.CONTINUE_ON_FAILURE)
