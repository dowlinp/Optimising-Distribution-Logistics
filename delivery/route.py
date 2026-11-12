
import json
import os
import requests
import flexpolyline
import xml.etree.ElementTree as ET


# -------------------------
# SETTINGS
# -------------------------

HERE_URL = "https://router.hereapi.com/v8/routes"

HERE_API_KEY = "XaPbKXY4U-eOd4Y7xTC9jbOUOJkLB_vkqQ9OHCKkdjg"

ORIGIN = "53.5,-6"
DESTINATION = "53,-6.5"

NS = "http://www.opengis.net/kml/2.2"


# -------------------------
# FETCH HERE ROUTE
# -------------------------

def get_here_data():

    if not HERE_API_KEY:
        raise ValueError("Missing HERE API key")

    response = requests.get(
        HERE_URL,
        params={
            "transportMode": "car",
            "origin": ORIGIN,
            "destination": DESTINATION,
            "return": "summary,incidents,polyline",
            "spans": "incidents,dynamicSpeedInfo,length",
            "apiKey": HERE_API_KEY
        },
        timeout=30
    )

    response.raise_for_status()

    return response.json()


# -------------------------
# CREATE KML
# -------------------------

def create_kml():

    ET.register_namespace("", NS)

    kml = ET.Element(f"{{{NS}}}kml")
    document = ET.SubElement(kml, f"{{{NS}}}Document")

    ET.SubElement(document, f"{{{NS}}}name").text = (
        "Delivery Route"
    )

    # Route style

    style = ET.SubElement(
        document,
        f"{{{NS}}}Style",
        id="route"
    )

    line_style = ET.SubElement(style, f"{{{NS}}}LineStyle")

    ET.SubElement(line_style, f"{{{NS}}}color").text = "ffff0000"
    ET.SubElement(line_style, f"{{{NS}}}width").text = "4"

    return kml, document


def add_folder(document, name):

    folder = ET.SubElement(document, f"{{{NS}}}Folder")

    ET.SubElement(folder, f"{{{NS}}}name").text = name

    return folder


def add_line(folder, name, description, coordinates, style):

    if len(coordinates) < 2:
        return

    placemark = ET.SubElement(
        folder,
        f"{{{NS}}}Placemark"
    )

    ET.SubElement(placemark, f"{{{NS}}}name").text = name
    ET.SubElement(placemark, f"{{{NS}}}description").text = description
    ET.SubElement(placemark, f"{{{NS}}}styleUrl").text = f"#{style}"

    line = ET.SubElement(
        placemark,
        f"{{{NS}}}LineString"
    )

    ET.SubElement(line, f"{{{NS}}}tessellate").text = "1"

    coords = " ".join(
        f"{lon},{lat},0" for lon, lat in coordinates
    )

    ET.SubElement(line, f"{{{NS}}}coordinates").text = coords


# -------------------------
# ADD HERE ROUTE TO KML
# -------------------------

def add_here_to_kml(document, data):

    routes_folder = add_folder(document, "HERE Delivery Route")

    for route in data.get("routes", []):

        for section in route.get("sections", []):

            if not section.get("polyline"):
                continue

            # Decode HERE's flexible polyline

            points = flexpolyline.decode(section["polyline"])

            coordinates = [
                (lon, lat) for lat, lon in points
            ]

            summary = section.get("summary", {})

            description = (
                f"Distance: {summary.get('length', 0)} metres\n"
                f"Duration: {summary.get('duration', 0)} seconds"
            )

            add_line(
                routes_folder,
                "Delivery Route",
                description,
                coordinates,
                "route"
            )


# -------------------------
# CREATE GEOJSON
# -------------------------

def add_geojson_line(features, name, description, coordinates):

    if len(coordinates) < 2:
        return

    features.append({
        "type": "Feature",
        "properties": {
            "name": name,
            "description": description,
            "type": "route"
        },
        "geometry": {
            "type": "LineString",
            "coordinates": coordinates
        }
    })


def add_here_to_geojson(features, data):

    for route in data.get("routes", []):

        for section in route.get("sections", []):

            if not section.get("polyline"):
                continue

            # Decode HERE's flexible polyline

            points = flexpolyline.decode(section["polyline"])

            # GeoJSON uses longitude, latitude

            coordinates = [
                [lon, lat] for lat, lon in points
            ]

            summary = section.get("summary", {})

            description = (
                f"Distance: {summary.get('length', 0)} metres\n"
                f"Duration: {summary.get('duration', 0)} seconds"
            )

            add_geojson_line(
                features,
                "Delivery Route",
                description,
                coordinates
            )


def save_geojson(features, order_id):

    geojson = {
        "type": "FeatureCollection",
        "features": features
    }

    os.makedirs("delivery/routes", exist_ok=True)

    filename = f"delivery/routes/order_{order_id}.geojson"

    with open(filename, "w", encoding="utf-8") as file:
        json.dump(geojson, file, indent=4)

    print(f"GeoJSON saved as {filename}")


# -------------------------
# MAIN
# -------------------------

def main():

    order_id = 123

    print("Fetching HERE route...")

    here_data = get_here_data()

    if not here_data.get("routes"):
        raise ValueError("HERE returned no routes")

    # Generate KML

    print("Creating KML...")

    kml, document = create_kml()

    add_here_to_kml(document, here_data)

    ET.ElementTree(kml).write(
        "traffic_map.kml",
        encoding="UTF-8",
        xml_declaration=True
    )

    print("KML saved as traffic_map.kml")

    # Generate GeoJSON

    print("Creating GeoJSON...")

    features = []

    add_here_to_geojson(features, here_data)

    save_geojson(features, order_id)


if __name__ == "__main__":
    main()
