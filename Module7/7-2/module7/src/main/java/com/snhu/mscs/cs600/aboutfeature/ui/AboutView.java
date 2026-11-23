
package com.snhu.mscs.cs600.aboutfeature.ui;

import com.snhu.mscs.cs600.aboutfeature.service.SunriseSunsetService;
import com.snhu.mscs.cs600.base.ui.component.ViewToolbar;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * AboutView -
 *
 * Purpose:
 *      Displays the About page and sunrise/sunset information.
 *      Users can choose a predefined location or enter their own
 *      coordinates. API communication is handled by a service.
 *
 * Version    Author          Date          Description
 * ****************************************************************
 * 1          fjm             20-Oct-2025   Initial Creation
 * 2          Chris Bright    05-Nov-2026   Added Sunrise/Sunset display
 * 3          Chris Bright    05-Nov-2026   Added API error handling
 * 4          Chris Bright    09-Nov-2026   Separated API service
 * 5          Chris Bright    09-Nov-2026   Added location selection
 */
@Route("")
@AnonymousAllowed
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "About")
public class AboutView extends VerticalLayout {

    /**
     * Stores a location name and its geographic coordinates.
     */
    private record Location(
        String name,
        Double latitude,
        Double longitude
    ) {}

    // Locations provided in the Module Seven assignment.
    private static final Location LINCOLN = new Location(
        "Lincoln Memorial, Washington DC",
        38.88941, -77.05013
    );

    private static final Location SEATTLE = new Location(
        "Seattle Space Needle, Washington",
        47.62061, -122.34926
    );

    private static final Location RUSHMORE = new Location(
        "Mount Rushmore, South Dakota",
        43.88051, -103.45374
    );

    private static final Location NIAGARA = new Location(
        "Niagara Falls, New York",
        43.077225, -79.075393
    );

    private static final Location MAUNA_KEA = new Location(
        "Mauna Kea Observatory, Hawaii",
        19.82761, -155.47418
    );

    private static final Location CUSTOM = new Location(
        "Custom coordinates", null, null
    );

    private final SunriseSunsetService sunriseSunsetService;

    // Location selection and optional coordinate inputs.
    private final ComboBox<Location> locationPicker =
        new ComboBox<>("Choose a location");

    private final NumberField latitude =
        new NumberField("Latitude");

    private final NumberField longitude =
        new NumberField("Longitude");

    private final HorizontalLayout customFields =
        new HorizontalLayout();

    // Components used to display the API results.
    private final H2 location = new H2();
    private final Span sunrise = new Span();
    private final Span sunset = new Span();
    private final Span feedback = new Span();

    /**
     * Creates the About page and receives the API service
     * through Spring dependency injection.
     */
    public AboutView(SunriseSunsetService sunriseSunsetService) {

        this.sunriseSunsetService = sunriseSunsetService;

        // Preserve the original About page content.
        StringBuilder text = new StringBuilder();

        text.append("<P>This is an example of a UI built with the Vaadin Framework.</P>\n");
        text.append("<P>This builds on the Lab from Module 3, utilizing the same concept ");
        text.append("of a list of names stored with Spring JPA in a PostgreSQL database.");
        text.append("<BR/><B><U>Notice:</U></B> In order to see or select the Names View, ");
        text.append("you must authenticate.</P>\n");

        Div content = new Div();
        content.getStyle()
            .set("display", "block")
            .set("white-space", "pre-wrap");

        content.getElement().setProperty(
            "innerHTML", text.toString()
        );

        // Keep the existing About page toolbar.
        add(new ViewToolbar("About"));

        // Configure the dropdown with available locations.
        locationPicker.setItems(
            LINCOLN,
            SEATTLE,
            RUSHMORE,
            NIAGARA,
            MAUNA_KEA,
            CUSTOM
        );

        locationPicker.setItemLabelGenerator(Location::name);
        locationPicker.setWidth("350px");
        locationPicker.setValue(LINCOLN);

        // Configure the optional custom coordinate fields.
        latitude.setMin(-90);
        latitude.setMax(90);
        latitude.setStep(0.00001);
        latitude.setWidth("170px");

        longitude.setMin(-180);
        longitude.setMax(180);
        longitude.setStep(0.00001);
        longitude.setWidth("170px");

        customFields.add(latitude, longitude);
        customFields.setVisible(false);

        // Show coordinate inputs only when Custom is selected.
        locationPicker.addValueChangeListener(event -> {

            boolean isCustom = CUSTOM.equals(event.getValue());

            customFields.setVisible(isCustom);
            feedback.setText("");
        });

        // Update the information when the button is clicked.
        Button update = new Button(
            "Update times",
            event -> updateSunTimes()
        );

        HorizontalLayout selection =
            new HorizontalLayout(locationPicker, update);

        selection.setAlignItems(FlexComponent.Alignment.END);

        // Display sunrise and sunset next to each other.
        HorizontalLayout times =
            new HorizontalLayout(sunrise, sunset);

        times.setSpacing(true);

        // Add all components to the About page.
        add(
            selection,
            customFields,
            location,
            times,
            feedback,
            content
        );

        // Load the default Lincoln Memorial information.
        updateSunTimes();
    }

    /**
     * Retrieves sunrise and sunset times for the selected location.
     * The View only handles input and display. The service handles
     * external communication and response processing.
     *
     * Author: Chris Bright
     */
    private void updateSunTimes() {

        Location selected = locationPicker.getValue();

        if (selected == null) {
            feedback.setText("Please select a location.");
            return;
        }

        Double lat;
        Double lng;
        String locationName;

        // Use the entered coordinates for a custom location.
        if (CUSTOM.equals(selected)) {

            lat = latitude.getValue();
            lng = longitude.getValue();

            if (lat == null || lng == null
                || !Double.isFinite(lat)
                || !Double.isFinite(lng)
                || lat < -90 || lat > 90
                || lng < -180 || lng > 180) {

                location.setText("Invalid coordinates");
                sunrise.setText("Sunrise: Unavailable");
                sunset.setText("Sunset: Unavailable");

                feedback.setText(
                    "Enter a latitude between -90 and 90 "
                        + "and a longitude between -180 and 180."
                );

                return;
            }

            locationName = "Custom Location (" + lat + ", " + lng + ")";

        } else {

            // Use the coordinates stored for the selected location.
            lat = selected.latitude();
            lng = selected.longitude();
            locationName = selected.name();
        }

        // Request the results from the separate service class.
        SunriseSunsetService.SunTimes result =
            sunriseSunsetService.getSunriseAndSet(lat, lng);

        // Update the displayed location and times.
        location.setText(locationName);
        sunrise.setText("Sunrise: " + result.sunrise());
        sunset.setText("Sunset: " + result.sunset());

        // Inform the user if the API request was unsuccessful.
        feedback.setText(result.isAvailable() ? ""
            : "Sunrise and sunset times could not be loaded. "
              + "Please try again later.");
    }
}
