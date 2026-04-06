import Foundation
import UIKit
import Photos

// MARK: - Screen Capture Function Namespace

enum ScreenCaptureFunctions {
    
    // MARK: - ScreenCapture.SaveBase64
    
    class SaveBase64: BridgeFunction {
        func execute(parameters: [String: Any]) throws -> [String: Any] {
            guard let rawData = parameters["data"] as? String, !rawData.isEmpty else {
                return ["success": false, "error": "'data' parameter is required"]
            }

            var filename = parameters["filename"] as? String
            let customPath = parameters["path"] as? String
            let qualityPercent = (parameters["quality"] as? NSNumber)?.intValue ?? 85
            let compressionQuality = CGFloat(max(1, min(100, qualityPercent))) / 100.0

            print("ScreenCapture.SaveBase64 - Saving image, quality: \(qualityPercent)")

            // Remove data URL prefix if present
            var base64Data = rawData
            if base64Data.contains(",") {
                base64Data = String(base64Data.split(separator: ",")[1])
            }

            // Decode base64
            guard let imageData = Data(base64Encoded: base64Data) else {
                return ["success": false, "error": "Invalid base64 data"]
            }

            // Compress image to JPEG to reduce memory usage before saving
            guard let image = UIImage(data: imageData),
                  let compressedData = image.jpegData(compressionQuality: compressionQuality) else {
                return ["success": false, "error": "Failed to compress image - may be too large for available memory"]
            }

            // Generate filename if not provided
            if filename == nil || filename?.isEmpty == true {
                let timestamp = DateFormatter()
                timestamp.dateFormat = "yyyyMMdd_HHmmss"
                filename = "screenshot_\(timestamp.string(from: Date())).jpg"
            }

            // Ensure .jpg extension
            if var fn = filename {
                let ext = (fn as NSString).pathExtension.lowercased()
                if ext != "jpg" && ext != "jpeg" {
                    fn = (fn as NSString).deletingPathExtension + ".jpg"
                }
                filename = fn
            }

            do {
                let savedPath: String

                if let customPath = customPath {
                    savedPath = try saveToCustomPath(imageData: compressedData, filename: filename!, path: customPath)
                } else {
                    savedPath = try saveToPhotoLibrary(imageData: compressedData, filename: filename!)
                }
                
                print("Image saved successfully: \(savedPath)")
                return [
                    "success": true,
                    "path": savedPath,
                    "filename": filename ?? ""
                ]
                
            } catch {
                print("Error saving image: \(error.localizedDescription)")
                return ["success": false, "error": error.localizedDescription]
            }
        }
        
        private func saveToPhotoLibrary(imageData: Data, filename: String) throws -> String {
            guard let image = UIImage(data: imageData) else {
                throw NSError(domain: "ScreenCapture", code: 1, userInfo: [NSLocalizedDescriptionKey: "Failed to create image from compressed data"])
            }

            let semaphore = DispatchSemaphore(value: 0)
            var saveError: Error?
            var localIdentifier: String?

            PHPhotoLibrary.requestAuthorization { status in
                guard status == .authorized else {
                    saveError = NSError(domain: "ScreenCapture", code: 2, userInfo: [NSLocalizedDescriptionKey: "Photo library access denied"])
                    semaphore.signal()
                    return
                }

                PHPhotoLibrary.shared().performChanges({
                    let request = PHAssetChangeRequest.creationRequestForAsset(from: image)
                    localIdentifier = request.placeholderForCreatedAsset?.localIdentifier
                }) { success, error in
                    if !success {
                        saveError = error
                    }
                    semaphore.signal()
                }
            }
            
            semaphore.wait()
            
            if let error = saveError {
                throw error
            }
            
            return "Photo Library: \(localIdentifier ?? filename)"
        }
        
        private func saveToCustomPath(imageData: Data, filename: String, path: String) throws -> String {
            let fileManager = FileManager.default
            let fileURL: URL
            
            if path.hasPrefix("/") {
                // Absolute path
                fileURL = URL(fileURLWithPath: path).appendingPathComponent(filename)
            } else {
                // Relative to documents directory
                let documentsPath = fileManager.urls(for: .documentDirectory, in: .userDomainMask)[0]
                fileURL = documentsPath.appendingPathComponent(path).appendingPathComponent(filename)
            }
            
            // Create directory if it doesn't exist
            let directory = fileURL.deletingLastPathComponent()
            if !fileManager.fileExists(atPath: directory.path) {
                try fileManager.createDirectory(at: directory, withIntermediateDirectories: true)
            }
            
            // Write file
            try imageData.write(to: fileURL)
            
            return fileURL.path
        }
    }
}