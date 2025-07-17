from flask import Flask, request, jsonify
import requests
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

GOOGLE_API_KEY = "AIzaSyCRIg6e5YVx4nznzkUlUpkXq_FtXOIzB38"
GOOGLE_CSE_ID = "501d31ec2746c4074"
YOUTUBE_API_KEY = "AIzaSyCRIg6e5YVx4nznzkUlUpkXq_FtXOIzB38"

def get_rasa_result(text):
    res = requests.post("http://localhost:5005/model/parse", json={"text": text})
    return res.json()

def translate(text):
    try:
        response = requests.post("http://localhost:9000/translate", json={"text": text})
        response.raise_for_status()
        return response.json()["translated_text"]
    except Exception as e:
        print(f"Translation API error: {e}")
        return text

def search_google(query):
    query = translate(query)
    print("In google the translated text : " + query)
    search_url = "https://www.googleapis.com/customsearch/v1"
    params = {
        "q": query,
        "key": GOOGLE_API_KEY,
        "cx": GOOGLE_CSE_ID
    }
    response = requests.get(search_url, params=params)
    return response.json().get("items", [])

def weather_search(query):
    query = translate(query)
    search_url = "https://www.googleapis.com/customsearch/v1"
    params = {
        "q": query,
        "key": GOOGLE_API_KEY,
        "cx": GOOGLE_CSE_ID
    }
    response = requests.get(search_url, params=params)
    return response.json().get("items", [])

def direction_search(query):
    query = translate(query)
    search_url = "https://www.googleapis.com/customsearch/v1"
    params = {
        "q": query,
        "key": GOOGLE_API_KEY,
        "cx": GOOGLE_CSE_ID
    }
    response = requests.get(search_url, params=params)
    return response.json().get("items", [])

def booking_search(query):
    query = translate(query)
    search_url = "https://www.googleapis.com/customsearch/v1"
    params = {
        "q": query,
        "key": GOOGLE_API_KEY,
        "cx": GOOGLE_CSE_ID
    }
    response = requests.get(search_url, params=params)
    return response.json().get("items", [])

def search_youtube(query):
    query = translate(query)
    print("In youtube the translated text : " + query)
    youtube_url = "https://www.googleapis.com/youtube/v3/search"
    params = {
        "part": "snippet",
        "q": query,
        "key": YOUTUBE_API_KEY,
        "type": "video",
        "maxResults": 3
    }
    try:
        response = requests.get(youtube_url, params=params)
        response.raise_for_status()
        results = response.json().get("items", [])

        videos = []
        for item in results:
            video_id = item["id"]["videoId"]
            video_title = item["snippet"]["title"]
            video_description = item["snippet"]["description"]
            video_channel = item["snippet"]["channelTitle"]
            video_link = f"https://www.youtube.com/watch?v={video_id}"
            videos.append({
                "title": video_title,
                "link": video_link,
                "descript": video_description,
                "channel": video_channel
            })
        return videos
    except requests.exceptions.RequestException as e:
        print(f"An error occurred: {e}")
        return []

@app.route("/process", methods=["POST"])
def process_input():
    data = request.get_json()
    user_text = data.get("text", "")

    if not user_text:
        return jsonify({"error": "No text provided"}), 400

    rasa_result = get_rasa_result(user_text)
    intent = rasa_result["intent"]["name"]
    confidence = rasa_result["intent"]["confidence"]
    entities = {ent["entity"]: ent["value"] for ent in rasa_result.get("entities", [])}
    query = entities.get("query", user_text)
    app_name = entities.get("app_name", user_text)              #UPdate 7/14 
    print("this is the intent : " + intent)
    print("The name of the app is  : " + app_name)

    if confidence < 0.5:
        return jsonify({
            "intent": "unknown", 
            "confidence": confidence, 
            "message": "माफ गर्नुहोस्, तपाईंको कुरा बुझ्न सकिन।"
            }), 200

    if intent == "greet":
        return jsonify({
            "intent": intent, 
            "reply": "नमस्ते! म तपाईंको सहयोगका लागि तयार छु।"
            })

    if intent == "goodbye":
        return jsonify({
            "intent": intent, 
            "reply": "Goodbye, see you again"
            })

    elif intent == "google_search":
        results = search_google(query)
        return jsonify({
            "intent": intent, 
            "results": results, 
            "message": "🔎 Google खोज परिणाम:", 
            "source": "google"
            })

    elif intent == "weather_search":
        results = weather_search(query)
        return jsonify({
            "intent": intent, 
            "results": results, 
            "message": "🔎 Google खोज परिणाम:", 
            "source": "google"
            })

    elif intent == "direction_search":
        results = direction_search(query)
        return jsonify({
            "intent": intent, 
            "results": results, 
            "message": "🔎 Google खोज परिणाम:", 
            "source": "google"
            })

    elif intent == "booking_search":
        results = booking_search(query)
        return jsonify({
            "intent": intent, 
            "results": results, 
            "message": "🔎 Google खोज परिणाम:", 
            "source": "google"
            })

    elif intent == "youtube_search":
        results = search_youtube(query)
        return jsonify({
            "intent": intent, 
            "results": results, 
            "message": "📺 YouTube भिडियो:", 
            "source": "youtube"
            })
    
    elif intent == "open_app":                  #UPdate 7/14
        app_name = translate(app_name)     #UPdate 7/14
        if app_name.lower() == "maze" :
            app_name = "messages"
        if app_name.lower() == "play story" :
            app_name = "playstore"
        if app_name.lower() == "photo" or app_name.lower() == "photos" :
            app_name = "photos"
        if app_name.lower() == "web browser" or app_name.lower() == "browser" or app_name.lower() == "google" :
            app_name = "chrome"
        if app_name.lower() == "google map" or app_name.lower() == "map" :
            app_name = "maps"

        print("The name of the app is after translation "+app_name.lower())  #UPdate 7/14
        return jsonify({
            "intent": intent, 
            "app_name": app_name,                #UPdate 7/14
            # "app_name": "facebook",               #UPdate 7/14
            "message": "Opening an app", 
            "source": "open_app"                #UPdate 7/14
            })

    return jsonify({
        "message": "माफ गर्नुहोस्, त्यो कार्य अहिले समर्थित छैन।"
        })

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8000, debug=True)
