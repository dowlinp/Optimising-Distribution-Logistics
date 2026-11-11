import os
 
from flask import Flask, jsonify, send_file
 
app = Flask(__name__)

@app.route("/")
def home():return send_file("Page.html")


if __name__ == "__main__":
    app.run(host=os.environ.get("HOST", "127.0.0.1"), port=5000,
    debug=os.environ.get("FLASK_DEBUG") == "1")