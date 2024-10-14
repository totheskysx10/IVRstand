from flask import Flask, request, send_file
from gtts import gTTS

app = Flask(__name__)


@app.route('/synthesize', methods=['POST'])
def synthesize():
    data = request.get_json()
    text = data.get('text', '')
    text = text.replace("\\n", "")
    text = text.replace("МФЦ", "эм фэ цэ")
    print(text)

    tts = gTTS(text=text, lang='ru')
    audio_file = 'tmp/output.wav'
    tts.save(audio_file)

    response = send_file(audio_file, as_attachment=True)
    return response

if __name__ == '__main__':
    app.run('0.0.0.0', port=5005)