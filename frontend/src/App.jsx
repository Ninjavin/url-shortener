import { useState } from "react";
import "./App.css";

function App() {
  const [longUrl, setLongUrl] = useState("");
  const [shortUrl, setShortUrl] = useState("");

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!longUrl.trim()) {
      setShortUrl("");
      return;
    }

    const response = await fetch("/api/v1/shorten", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        longUrl: longUrl,
      }),
    });

    const shortUrl = await response.json();
    console.log(shortUrl);
    setShortUrl(shortUrl["shortCode"]);
    // setShortUrl('https://sho.rt/abc123')
  };

  return (
    <div className="app">
      <header className="header">
        <h1>URL Shortener</h1>
        <p>Paste a long URL and get a shorter link.</p>
      </header>

      <form className="card" onSubmit={handleSubmit}>
        <label htmlFor="longUrl">Long URL</label>
        <div className="row">
          <input
            id="longUrl"
            type="url"
            placeholder="https://example.com/very/long/url"
            value={longUrl}
            onChange={(event) => setLongUrl(event.target.value)}
            required
          />
          <button type="submit">Shorten</button>
        </div>
        <div className="output">
          <span>Short URL</span>
          <output>{shortUrl || "—"}</output>
        </div>
      </form>
    </div>
  );
}

export default App;
