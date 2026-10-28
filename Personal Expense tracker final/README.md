# Moneta — Drag-and-Drop Personal Finance Manager

A polished personal finance dashboard built with HTML, CSS and JavaScript. This edition uses the browser's **localStorage** for transactions and monthly budgets. It requires no Java server, API token, database key, GitHub repository, build command or separate backend host.

## Deploy using Vercel Drop

1. Extract this ZIP file.
2. Open the extracted `Moneta_Drag_Drop_Ready` folder.
3. Visit https://vercel.com/drop and sign in.
4. Drag the extracted folder (the folder containing `index.html`, `styles.css` and `app.js`) into Vercel Drop.
5. Complete the deployment prompts. Your website will be published as a static site.

Do not drag only `app.js`; the HTML and CSS files are required too.

## Run locally

You can open `index.html` directly in a modern browser. For a local web-server preview, open a terminal in this folder and run:

```bash
python -m http.server 5500
```

Then visit http://localhost:5500.

## Features

- Income and expense entries
- Automatic balance, income and expense totals
- Transaction history with search and filters
- Category-wise spending breakdown
- Six-month income-versus-expense chart
- Monthly budget with progress and over-budget warning
- Delete transactions
- Export filtered transactions to CSV
- Responsive dashboard layout

## Where your data is saved

Transactions and the monthly budget are saved in the browser using `localStorage`.

**Important limitations:**
- Data is saved only in the browser profile and device where it was entered.
- It does not automatically sync between phones, computers or different browsers.
- Clearing browser site data can delete saved transactions.
- Private/incognito browsing may clear data when the session ends.
- This is suitable for a single-user college demonstration, not a production finance service.

To keep a backup, use **Export CSV** in the Transactions section periodically.

## About the Java files

The `java-viva-reference` folder contains the original Core Java backend source files for studying and discussing Java OOP concepts in your viva. They are included as reference material only; this Vercel Drop edition does not execute them. The deployed application runs entirely in the browser and uses localStorage instead of a Java backend.

If your college requires a working Java backend as part of the submitted project, keep and demonstrate the separate Java version locally. This drag-and-drop edition is the no-server deployment version.
