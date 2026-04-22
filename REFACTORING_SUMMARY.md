# Dataset Refactoring Summary

## Changes Made

### New Class Created: `DataManager.java`

This new class handles all dataset-related operations:

**Responsibilities:**
- Loading CSV dataset files
- Storing raw dataset and train/validation/test splits
- Computing normalization statistics (min/max values)
- Applying normalization to datasets and individual features
- Managing normalization state

**Key Methods:**
- `loadDataset(File file)` - Load and split dataset
- `getTrainDataForModel(boolean normalize)` - Get processed training data
- `getValDataForModel(boolean normalize)` - Get processed validation data
- `getTestDataForModel(boolean normalize)` - Get processed test data
- `applyNormalizationToFeatures(double soil, double last, int type)` - Normalize plant input features
- `setNormalizationEnabled(boolean)` - Set normalization flag
- `isDatasetLoaded()` - Check if dataset is loaded

### Updated `main.java` (gui class)

**Removed:**
- Dataset-related fields (rawDataset, rawTrainData, rawValData, rawTestData, normalizationEnabled, soilMin, soilMax, lastMin, lastMax, selectedDatasetFile)
- Dataset-related methods (loadDatasetFromFile, splitDataset, cloneDataset, normalizeDataset, computeNormalizationStats, normalizeValue)

**Added:**
- New field: `DataManager dataManager`
- Instantiated DataManager in constructor

**Updated:**
- `chooseDatasetFile()` - Now calls `dataManager.loadDataset()`
- `trainModel()` - Now calls `dataManager.getTrainDataForModel()`, `dataManager.getValDataForModel()`, etc.
- `applyNormalizationToPlantFeatures()` - Now delegates to `dataManager.applyNormalizationToFeatures()`
- `updateTrainingStatusUI()` - Now calls `dataManager.isDatasetLoaded()`, `dataManager.getDatasetSize()`, etc.

## Behavior Preserved

✅ All existing functionality works identically
✅ Same training workflow
✅ Same normalization logic
✅ Same dataset splitting (70% train, 15% validation, 15% test)
✅ Same plant prediction flow
✅ Same GUI behavior

## Code Quality Improvements

- Better separation of concerns
- Dataset logic is now isolated and reusable
- GUI class is cleaner and more focused
- Easier to test dataset operations independently
- Future enhancements to dataset handling won't clutter the GUI class

## Files Modified

1. **Created:** `src/main/java/DataManager.java` (201 lines)
2. **Modified:** `src/main/java/main.java` (removed ~100 lines of dataset logic)

## How to Use

The refactoring is complete and ready to use. Simply:
1. Place `DataManager.java` in your project
2. Replace your existing `main.java` with the updated version
3. No other changes needed—everything works as before

