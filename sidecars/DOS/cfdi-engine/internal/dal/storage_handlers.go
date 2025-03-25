package dal

import (
	"context"
	"errors"
	"fmt"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"

	"immortalcrabcorp.com/fiscal-engine/internal/dal/models"
)

// CreateReceipt inserts a new Receipt into MongoDB.
func CreateReceipt(db *mongo.Database, receipt *models.Receipt) (primitive.ObjectID, error) {
	collection := db.Collection("receipts")

	// Ensure that the receipt doesn't already have an ID (new documents should not have one)
	if receipt.ID != primitive.NilObjectID {
		return primitive.NilObjectID, errors.New("receipt already has an ID; cannot create")
	}

	// Generate a new ObjectID for the new receipt
	receipt.ID = primitive.NewObjectID()

	// Insert the new receipt document into the collection
	result, err := collection.InsertOne(context.TODO(), receipt)
	if err != nil {
		return primitive.NilObjectID, err
	}

	// Return the generated ObjectID
	oid, ok := result.InsertedID.(primitive.ObjectID)
	if !ok {
		return primitive.NilObjectID, fmt.Errorf("failed to retrieve generated ObjectID")
	}
	return oid, nil
}

// EditReceipt updates an existing Receipt in MongoDB.
func EditReceipt(db *mongo.Database, receipt *models.Receipt) error {
	collection := db.Collection("receipts")

	if receipt.ID == primitive.NilObjectID {
		return errors.New("receipt ID is required for editing")
	}

	// Filter to find the receipt by ID
	filter := bson.M{"_id": receipt.ID}

	// Update the fields of the receipt document
	update := bson.M{
		"$set": bson.M{
			"owner":             receipt.Owner,
			"receptor_rfc":      receipt.ReceptorRFC,
			"receptor_data_ref": receipt.ReceptorDataRef,
			"document_currency": receipt.DocumentCurrency,
			"base_currency":     receipt.BaseCurrency,
			"exchange_rate":     receipt.ExchangeRate,
			"items":             receipt.Items, // Updating the items with taxes
			"purpose":           receipt.Purpose,
			"payment_way":       receipt.PaymentWay,
			"payment_method":    receipt.PaymentMethod,
			"serie":             receipt.Serie,
			"comments":          receipt.Comments,
			"chronology":        receipt.Chronology,
		},
	}

	// Perform the update without upsert
	result, err := collection.UpdateOne(context.TODO(), filter, update, options.Update().SetUpsert(false))
	if err != nil {
		return err
	}

	// If no documents were matched, return an error
	if result.MatchedCount == 0 {
		return errors.New("no receipt found with the given ID")
	}

	return nil
}

// GetReceiptByID retrieves a Receipt by its ID (as a string) from MongoDB.
func GetReceiptByID(db *mongo.Database, receiptID string) (*models.Receipt, error) {
	collection := db.Collection("receipts")

	// Convert the string ID to ObjectID
	objID, err := primitive.ObjectIDFromHex(receiptID)
	if err != nil {
		return nil, errors.New("invalid receipt ID format")
	}

	// Create a context with a timeout for the database operation
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	// Prepare the filter to find the receipt by ID
	filter := bson.M{"_id": objID}
	receipt := &models.Receipt{}

	// Find the receipt in the collection
	err = collection.FindOne(ctx, filter).Decode(receipt)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return nil, errors.New("receipt not found")
		}
		return nil, err
	}

	return receipt, nil
}

func updateChronologyField(db *mongo.Database, receiptID string, field string, at time.Time) error {
	collection := db.Collection("receipts")

	objectId, err := primitive.ObjectIDFromHex(receiptID)
	if err != nil {
		return fmt.Errorf("invalid ObjectID: %v", err)
	}

	// Construct the filter dynamically based on the provided field
	filter := bson.M{
		"_id": objectId,
		"$or": []bson.M{
			{fmt.Sprintf("chronology.%s", field): bson.M{"$exists": false}}, // Field does not exist
			{fmt.Sprintf("chronology.%s", field): nil},                      // Field is null
		},
	}

	// Update operation
	update := bson.M{"$set": bson.M{fmt.Sprintf("chronology.%s", field): at}}

	// Attempt to update the document
	result, err := collection.UpdateOne(context.TODO(), filter, update)
	if err != nil {
		return fmt.Errorf("failed to update %s field: %v", field, err)
	}

	if result.MatchedCount == 0 {
		return fmt.Errorf("field 'chronology.%s' already has a value", field)
	}

	return nil
}

func squashChronologyField(db *mongo.Database, receiptID string, field string, at time.Time) error {
	collection := db.Collection("receipts")

	objectId, err := primitive.ObjectIDFromHex(receiptID)
	if err != nil {
		return fmt.Errorf("invalid ObjectID: %v", err)
	}

	// Construct the filter dynamically based on the provided field
	filter := bson.M{
		"_id": objectId,
	}

	// Update operation
	update := bson.M{"$set": bson.M{fmt.Sprintf("chronology.%s", field): at}}

	// Attempt to update the document
	_, err = collection.UpdateOne(context.TODO(), filter, update)
	if err != nil {
		return fmt.Errorf("failed to squash %s field: %v", field, err)
	}

	return nil
}

func updateDocUUIDField(db *mongo.Database, receiptID string, docUUID string) error {
	collection := db.Collection("receipts")

	objectID, err := primitive.ObjectIDFromHex(receiptID)
	if err != nil {
		return fmt.Errorf("invalid ObjectID: %v", err)
	}

	// Construct the filter to find the document by _id
	filter := bson.M{"_id": objectID}

	// Define the update to set the pac_doc_uuid_ref in the meta section
	update := bson.M{
		"$set": bson.M{
			"meta.pac_doc_uuid_ref": docUUID,
		},
	}

	// Perform the update
	updateResult, err := collection.UpdateOne(context.TODO(), filter, update)
	if err != nil {
		return fmt.Errorf("failed to update document: %w", err)
	}

	// Check if the document was found and updated
	if updateResult.MatchedCount == 0 {
		return fmt.Errorf("no document found with receiptID %s", receiptID)
	}

	return nil
}

func UpdateStartedAt(db *mongo.Database, receiptID string, at time.Time) error {
	return updateChronologyField(db, receiptID, "started_at", at)
}

func UpdateCanceledAt(db *mongo.Database, receiptID string, at time.Time) error {
	return updateChronologyField(db, receiptID, "cancelled_at", at)
}

func UpdateCancelAttemptStartedAt(db *mongo.Database, receiptID string, at time.Time) error {
	return squashChronologyField(db, receiptID, "cancel_attempt_started_at", at)
}

func UpdateCancelFailedAttempt(db *mongo.Database, receiptID string, at time.Time) error {
	return squashChronologyField(db, receiptID, "cancel_attempt_failed_at", at)
}

func UpdateCompletedAt(db *mongo.Database, receiptID string, at time.Time, docUUID string) error {
	if err := updateChronologyField(db, receiptID, "completed_at", at); err != nil {
		return err
	}
	if err := updateDocUUIDField(db, receiptID, docUUID); err != nil {
		return err
	}
	return nil
}

func UpdateNonCompletedAt(db *mongo.Database, receiptID string, at time.Time) error {
	return updateChronologyField(db, receiptID, "non_completed_at", at)
}

func DeleteReceiptByID(db *mongo.Database, receiptID string) error {
	collection := db.Collection("receipts")

	// Convert the string ID to MongoDB's ObjectID type
	objectId, err := primitive.ObjectIDFromHex(receiptID)
	if err != nil {
		return fmt.Errorf("invalid ObjectID: %v", err)
	}

	// Filter for the document to delete
	filter := bson.M{"_id": objectId}

	// Delete the document
	result, err := collection.DeleteOne(context.TODO(), filter)
	if err != nil {
		return fmt.Errorf("failed to delete document: %v", err)
	}

	if result.DeletedCount == 0 {
		return fmt.Errorf("no document found with ID: %s", receiptID)
	}

	return nil
}

func DeleteFutileReceiptByID(db *mongo.Database, receiptID string) (bool, error) {
	collection := db.Collection("receipts")
	oid, err := primitive.ObjectIDFromHex(receiptID)
	if err != nil {
		return false, fmt.Errorf("ID invalid: %v", err)
	}

	filter := bson.M{
		"_id": oid,
		"meta": bson.M{
			"$eq": bson.M{},
		},
		"chronology": bson.M{
			"$exists": true,
		},
		"chronology.defer_at": bson.M{
			"$exists": false,
		},
	}

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	result, err := collection.DeleteOne(ctx, filter)

	if result.DeletedCount == 0 {
		return false, nil
	} else if err != nil {
		return false, fmt.Errorf("error trying to delete document: %w", err)
	}
	return true, nil
}
