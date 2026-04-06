import Foundation
import UIKit

// MARK: - Browser Open Function Namespace

enum BrowserOpenFunctions {

    // MARK: - BrowserOpen.Open

    class Open: BridgeFunction {
        func execute(parameters: [String: Any]) throws -> [String: Any] {
            guard let urlString = parameters["url"] as? String, !urlString.isEmpty else {
                return ["success": false, "error": "'url' parameter is required"]
            }

            guard let url = URL(string: urlString) else {
                return ["success": false, "error": "Invalid URL format"]
            }

            // Reject non-http(s) schemes for safety
            let scheme = url.scheme?.lowercased()
            guard scheme == "http" || scheme == "https" else {
                return ["success": false, "error": "Only http and https URLs are supported"]
            }

            print("BrowserOpen.Open - Opening URL: \(urlString)")

            var result: [String: Any] = ["success": false, "error": "Unable to open URL"]
            let semaphore = DispatchSemaphore(value: 0)

            DispatchQueue.main.async {
                UIApplication.shared.open(url, options: [:]) { success in
                    if success {
                        print("BrowserOpen.Open - Browser launched successfully")
                        result = ["success": true]
                    } else {
                        print("BrowserOpen.Open - Failed to open URL")
                        result = ["success": false, "error": "Failed to open URL in browser"]
                    }
                    semaphore.signal()
                }
            }

            semaphore.wait()
            return result
        }
    }
}
