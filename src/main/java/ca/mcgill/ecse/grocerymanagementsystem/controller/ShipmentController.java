package ca.mcgill.ecse.grocerymanagementsystem.controller;

import ca.mcgill.ecse.grocerymanagementsystem.model.GroceryManagementSystem;
import ca.mcgill.ecse.grocerymanagementsystem.model.Item;
import ca.mcgill.ecse.grocerymanagementsystem.model.Shipment;
import ca.mcgill.ecse.grocerymanagementsystem.model.ShipmentItem;
import java.sql.Date;

public class ShipmentController {

	public static void createShipment() {
		GroceryManagementSystem system = GroceryManagementSystemController.getGroceryManagementSystem();
		new Shipment(null, system);
	}

	private static boolean checkIfOrdered(Shipment shipment){
		Date today = new Date(System.currentTimeMillis());
		return !shipment.getDateOrdered().after(today);
	}

	private static Shipment retrieveShipment(int shipmentNumber){
		GroceryManagementSystem system = GroceryManagementSystemController.getGroceryManagementSystem();
		for (Shipment s : system.getShipments()) {
			if (s.getShipmentNumber() == shipmentNumber){
				return s;
			}
		}
		throw new GroceryStoreException(String.format("there is no shipment with number %d", shipmentNumber));
	}

	public static void deleteShipment(int shipmentNumber) {
		Shipment shipment = retrieveShipment(shipmentNumber);
		if (checkIfOrdered(shipment)){
			throw new GroceryStoreException("cannot delete a shipment which has already been ordered");
		}
		shipment.delete();
	}

	private static Item retrieveItem(String itemName){
		GroceryManagementSystem system = GroceryManagementSystemController.getGroceryManagementSystem();
		for (Item i : system.getItems()){
			if (i.getName().equals(itemName)){
				return i;
			}
		}
		throw new GroceryStoreException(String.format("there is no item called %s", itemName));
	}

	public static void addItemToShipment(int shipmentNumber, String itemName) {
		//look for shipment with <shipmentNumber>
		Shipment ship = retrieveShipment(shipmentNumber);
		//look for itemName
		Item item = retrieveItem(itemName);
		//look for already existing item in shipment
		for (ShipmentItem i : ship.getShipmentItems()){
			if (i.getItem() == item){
				throw new GroceryStoreException(String.format("shipment already includes item %s", itemName));
			}
		}
		//check if shipment has already been ordered
		if (checkIfOrdered(ship)){
			throw new GroceryStoreException("shipment has already been ordered");
		}

		//add item to ShipmentItems
		new ShipmentItem(1, ship.getGroceryManagementSystem(), ship, item);
	}

	public static void updateQuantityInShipment(int shipmentNumber, String itemName, int newQuantity) {
		Shipment ship = retrieveShipment(shipmentNumber);
		ShipmentItem shipItem = null;
		for (ShipmentItem i : ship.getShipmentItems()){
			if (i.getItem().getName().equals(itemName)){
				shipItem = i;
				break;
			}
		}

		if (shipItem == null){
			throw new GroceryStoreException(String.format("shipment does not include item %s", itemName));
		}

		if (newQuantity < 0){
			throw new GroceryStoreException("quantity must be non-negative");
		}

		if (checkIfOrdered(ship)){
			throw new GroceryStoreException("shipment has already been ordered");
		}

		if (newQuantity == 0){
			shipItem.delete();
		}else{
			shipItem.setQuantity(newQuantity);
		}
	}
}
