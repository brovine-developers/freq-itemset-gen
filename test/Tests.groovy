import groovy.io.FileType

/**
 * Test harness: Runs all tests under test directory
 * (files with suffix *Test.groovy)
 *
 * Starts each test script with the file name, ends with "done"
 *
 * @author tcirwin
 */
List<File> list = []

def dir = new File(".")
dir.eachFileRecurse (FileType.FILES) { file ->
    if (file.name.endsWith("Test.groovy"))
        list << file
}

for (def file : list) {
    print(file.name + "...")
    evaluate(file)
    println("done")
}