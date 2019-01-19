package com.schentrupsoftware.mard.Objects;
import com.schentrupsoftware.mard.CurrentLocation;

import java.util.Date;

public class TagUpdate {

    public enum Sex {
        Male,
        Female
    }

    public enum Color {
        Red,
        Blue,
        Green,
        Gray,
        Orange,
        Purple
    }

    public String tagID;
    public Sex sex;
    public Color color;
    public String species;

    public Date timeOfObservation;
    public Double longitudeOfObservation;
    public Double latitudeOfObservation;

    public TagUpdate(String tagID, String sex, String color, String species, CurrentLocation currentLocation) {
        timeOfObservation = new Date();
        longitudeOfObservation = currentLocation.getLongitude();
        latitudeOfObservation = currentLocation.getLatitude();

        this.tagID = tagID;
        this.species = species;

        switch (sex) {
            case "Male":
                this.sex = Sex.Male;
                break;
            case "Female":
                this.sex = Sex.Female;
                break;
            default:
                System.out.print("Tag Update: Invalid sex: " + sex);
        }

        switch (color) {
            case "Red":
                this.color = Color.Red;
                break;
            case "Blue":
                this.color = Color.Blue;
                break;
            case "Green":
                this.color = Color.Green;
                break;
            case "Gray":
                this.color = Color.Gray;
                break;
            case "Orange":
                this.color = Color.Orange;
                break;
            case "Purple":
                this.color = Color.Purple;
                break;
            default:
                System.out.print("Tag Update: Invalid color: " + color);
        }
    }

    public String toString() {
        return "{ " + tagID + ", " + sex + ", " + color + ", " + species + ", " + timeOfObservation + ", " + longitudeOfObservation + ", " + latitudeOfObservation + " }";
    }
}
