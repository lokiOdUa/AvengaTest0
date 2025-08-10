package info.dvad.avengatest.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ExtentReportManager implements ITestListener {
    public ExtentReports extent;
    public ExtentSparkReporter spark;
    public ExtentTest test;

    public void onStart(ITestContext context) {
        String repName = "report-" + System.currentTimeMillis() + ".html";
        spark = new ExtentSparkReporter(System.getProperty("user.dir") + "/reports/" + repName);

        spark.config().setDocumentTitle("Avenga FakeRESTApi Test Report");
        spark.config().setReportName("Avenga FakeRESTApi Test Report");
        spark.config().setTheme(Theme.DARK);

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Application", "FakeRESTApi");
        extent.setSystemInfo("Operating System", System.getProperty("os.name"));
        extent.setSystemInfo("User Name", System.getProperty("user.name"));
    }

    public void onTestSuccess(ITestResult result) {
        test = extent.createTest(result.getName());
        test.pass("Test Passed");
    }

    public void onTestFailure(ITestResult result) {
        test = extent.createTest(result.getName());
        test.fail("Test Failed");
        test.fail(result.getThrowable());
    }

    public void onTestSkipped(ITestResult result) {
        test = extent.createTest(result.getName());
        test.skip("Test Skipped");
        test.skip(result.getThrowable());
    }

    public void onFinish(ITestContext context) {
        extent.flush();
    }
}
