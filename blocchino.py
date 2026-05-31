import tkinter as tk
from tkinter import filedialog, messagebox
import webbrowser
import os

# --- LOGICA PERCORSO (Iniziale Blindata OneDrive) ---
def ottieni_percorso_iniziale():
    p = "/mnt/c/Users/aless/OneDrive/Desktop/IFV/Documenti"
    if os.path.exists(p): return p
    home = os.path.expanduser("~")
    doc_linux = os.path.join(home, "Documents")
    os.makedirs(doc_linux, exist_ok=True)
    return doc_linux

# --- CONFIGURAZIONE ---
font_size = [12]
cartella_safe = [ottieni_percorso_iniziale()]
file_corrente = [os.path.join(cartella_safe[0], "Blocchino_AutoSave.txt")]

# --- LOGICA AUTO-SAVE (Ottimizzata nativamente senza Thread instabili) ---
def auto_save_sentinel():
    if auto_save_attivo.get() and file_corrente[0]:
        try:
            contenuto = text_area.get(1.0, tk.END)
            with open(file_corrente[0], "w", encoding="utf-8") as f:
                f.write(contenuto)
            aggiorna_stato("✅ Backup automatico eseguito")
        except Exception as e:
            aggiorna_stato("❌ Errore Auto-Save")
    # Riesegue la funzione in modo sicuro ogni 30 secondi (30000 millisecondi)
    finestra.after(30000, auto_save_sentinel)

def aggiorna_stato(messaggio):
    label_stato.config(text=messaggio)
    finestra.after(5000, lambda: label_stato.config(text="Pronto"))

# --- FINESTRA IMPOSTAZIONI ---
def apri_impostazioni():
    win_settings = tk.Toplevel(finestra)
    win_settings.title("Impostazioni")
    win_settings.geometry("400x250")
    win_settings.resizable(False, False)

    tk.Label(win_settings, text="Configurazione Blocchino", font=("Arial", 12, "bold")).pack(pady=10)

    lbl_percorso = tk.Label(win_settings, text=f"Attuale: {cartella_safe[0]}", wraplength=350, fg="blue")
    lbl_percorso.pack(pady=5)

    def cambia_percorso():
        nuova_cartella = filedialog.askdirectory()
        if nuova_cartella:
            cartella_safe[0] = nuova_cartella
            file_corrente[0] = os.path.join(cartella_safe[0], "Blocchino_AutoSave.txt")
            lbl_percorso.config(text=f"Attuale: {cartella_safe[0]}")
            aggiorna_stato("📍 Percorso aggiornato!")

    tk.Button(win_settings, text="Cambia Cartella", command=cambia_percorso).pack(pady=5)
    tk.Button(win_settings, text="Chiudi", command=win_settings.destroy).pack(pady=10)

# --- FUNZIONE TROVA E SOSTITUISCI ALPHA (Novità v3.0.0-alpha.1) ---
def apri_trova_sostituisci(event=None):
    win_find = tk.Toplevel(finestra)
    win_find.title("Trova e Sostituisci")
    win_find.geometry("420x180")
    win_find.resizable(False, False)
    win_find.transient(finestra) # Resta sopra la finestra principale

    tk.Label(win_find, text="Trova:").grid(row=0, column=0, padx=10, pady=10, sticky="w")
    entry_trova = tk.Entry(win_find, width=30)
    entry_trova.grid(row=0, column=1, padx=10, pady=10)
    entry_trova.focus_set()

    tk.Label(win_find, text="Sostituisci con:").grid(row=1, column=0, padx=10, pady=10, sticky="w")
    entry_sostituisci = tk.Entry(win_find, width=30)
    entry_sostituisci.grid(row=1, column=1, padx=10, pady=10)

    # Rimuove l'evidenziazione precedente quando si chiude la finestra
    def pulisci_e_chiudi():
        text_area.tag_remove("match", "1.0", tk.END)
        win_find.destroy()
    win_find.protocol("WM_DELETE_WINDOW", pulisci_e_chiudi)

    def esegui_trova():
        text_area.tag_remove("match", "1.0", tk.END)
        parola = entry_trova.get()
        if not parola:
            aggiorna_stato("⚠️ Inserisci una parola da cercare!")
            return

        inizio = "1.0"
        contatore = 0
        while True:
            inizio = text_area.search(parola, inizio, stopindex=tk.END)
            if not inizio: break
            fine = f"{inizio}+{len(parola)}c"
            text_area.tag_add("match", inizio, fine)
            if contatore == 0:
                text_area.see(inizio) # Scorre la visuale sul primo match
            inizio = fine
            contatore += 1

        text_area.tag_config("match", background="yellow", foreground="black")
        aggiorna_stato(f"🔍 Trovati {contatore} riscontri per '{parola}'")

    def esegui_sostituisci():
        parola = entry_trova.get()
        nuova_parola = entry_sostituisci.get()
        if not parola: return

        # Trova il primo match disponibile a partire dall'inizio
        inizio = text_area.search(parola, "1.0", stopindex=tk.END)
        if inizio:
            fine = f"{inizio}+{len(parola)}c"
            text_area.delete(inizio, fine)
            text_area.insert(inizio, nuova_parola)
            esegui_trova() # Riesegue il calcolo per aggiornare i tag evidenziati
            aggiorna_stato("✅ Sostituzione eseguita")
        else:
            aggiorna_stato("⚠️ Nessun match trovato da sostituire!")

    def esegui_sostituisci_tutto():
        parola = entry_trova.get()
        nuova_parola = entry_sostituisci.get()
        if not parola: return

        contenuto = text_area.get(1.0, "end-1c")
        contatore = contenuto.count(parola)
        
        if contatore > 0:
            nuovo_contenuto = contenuto.replace(parola, nuova_parola)
            text_area.delete(1.0, tk.END)
            text_area.insert(1.0, nuovo_contenuto)
            aggiorna_conteggio()
            aggiorna_stato(f"⚡ Sostituite {contatore} occorrenze!")
            text_area.tag_remove("match", "1.0", tk.END)
        else:
            aggiorna_stato("⚠️ Parola non trovata!")

    # Layout Pulsanti
    frame_btn = tk.Frame(win_find)
    frame_btn.grid(row=2, column=0, columnspan=2, pady=15)

    tk.Button(frame_btn, text="Trova", command=esegui_trova, width=10).pack(side=tk.LEFT, padx=5)
    tk.Button(frame_btn, text="Sostituisci", command=esegui_sostituisci, width=10).pack(side=tk.LEFT, padx=5)
    tk.Button(frame_btn, text="Sostituisci Tutto", command=esegui_sostituisci_tutto, width=13).pack(side=tk.LEFT, padx=5)

# --- FUNZIONI DI FILE ---
def nuovo_file(event=None):
    if text_area.get(1.0, "end-1c").strip():
        risposta = messagebox.askyesnocancel("Nuovo File", "Vuoi salvare prima?")
        if risposta is True: salva_file()
        elif risposta is None: return "break"
    text_area.delete(1.0, tk.END)
    file_corrente[0] = os.path.join(cartella_safe[0], "Blocchino_AutoSave.txt")
    aggiorna_conteggio()
    aggiorna_stato("Nuovo documento creato")
    return "break"

def apri_file(event=None):
    path = filedialog.askopenfilename(filetypes=[("File di testo", "*.txt")])
    if path:
        with open(path, "r", encoding="utf-8") as f:
            text_area.delete(1.0, tk.END)
            text_area.insert(tk.END, f.read())
            file_corrente[0] = path
            aggiorna_conteggio()
            aggiorna_stato(f"Aperto: {os.path.basename(path)}")
    return "break"

def salva_file(event=None):
    if "Blocchino_AutoSave.txt" in file_corrente[0] or not file_corrente[0]:
        path = filedialog.asksaveasfilename(defaultextension=".txt", filetypes=[("File di testo", "*.txt")])
        if path: file_corrente[0] = path
        else: return "break"
    with open(file_corrente[0], "w", encoding="utf-8") as f:
        f.write(text_area.get(1.0, tk.END))
    aggiorna_stato("💾 Salvataggio completato")
    return "break"

def conferma_uscita(event=None):
    if text_area.get(1.0, "end-1c").strip():
        risposta = messagebox.askyesnocancel("Esci", "Vuoi salvare prima di uscire?")
        if risposta is True: 
            salva_file()
            finestra.destroy()
        elif risposta is False: 
            finestra.destroy()
    else: 
        finestra.destroy()
    return "break"

# --- FUNZIONI FORMATO E ZOOM ---
def zoom_in(event=None):
    font_size[0] += 2
    text_area.config(font=("Arial", font_size[0]))
    return "break"

def zoom_out(event=None):
    if font_size[0] > 6:
        font_size[0] -= 2
        text_area.config(font=("Arial", font_size[0]))
    return "break"

def reset_zoom(event=None):
    font_size[0] = 12
    text_area.config(font=("Arial", font_size[0]))
    return "break"

def testo_maiuscolo():
    try:
        start, end = text_area.index("sel.first"), text_area.index("sel.last")
        txt = text_area.get(start, end).upper()
        text_area.delete(start, end)
        text_area.insert(start, txt)
        aggiorna_conteggio()
    except: 
        aggiorna_stato("⚠️ Seleziona testo!")

def testo_minuscolo():
    try:
        start, end = text_area.index("sel.first"), text_area.index("sel.last")
        txt = text_area.get(start, end).lower()
        text_area.delete(start, end)
        text_area.insert(start, txt)
        aggiorna_conteggio()
    except: 
        aggiorna_stato("⚠️ Seleziona testo!")

# --- SUPPORTO E GUIDA ---
def mostra_guida():
    guida_testo = (
        "Comandi Rapidi di Blocchino:\n"
        "---------------------------\n"
        "• Ctrl+N : Nuovo Documento\n"
        "• Ctrl+O : Apri File esistente\n"
        "• Ctrl+S : Salva Manuale\n"
        "• F4 o Ctrl+F : Trova e Sostituisci 🔍\n"
        "• Ctrl+Plus/Minus : Regola lo Zoom\n\n"
        "SALVATAGGIO AUTOMATICO:\n"
        "Attiva 'Il dittatore Salvatore' nel menu File.\n"
        "Salva ogni 30 secondi nella tua cartella DOCUMENTI.\n"
        "Percorso attuale: {cartella_safe[0]}\n"
        "Puoi cambiare il percorso in Modifica -> Impostazioni ⚙️!"
    )
    messagebox.showinfo("Guida all'uso", guida_testo)

def vai_al_sito():
    webbrowser.open("https://saffdds.github.io/bellum_iustum/aiuto_blocchino.html")

def informazioni_software():
    messagebox.showinfo("Informazioni", "Blocchino v3.0.0a1\nAutore: Saffdds\nStato: Sviluppo Alpha")

def aggiorna_conteggio(event=None):
    contenuto = text_area.get(1.0, "end-1c")
    numero_caratteri = len(contenuto)
    label_caratteri.config(text=f"Caratteri: {numero_caratteri}")

# --- INTERFACCIA ---
finestra = tk.Tk()
finestra.title("Blocchino")
finestra.geometry("700x600")

auto_save_attivo = tk.BooleanVar(value=False)
text_area = tk.Text(finestra, wrap="word", font=("Arial", font_size[0]), undo=True)
text_area.pack(expand=True, fill="both")

# --- NUOVA BARRA DI STATO CONDIVISA ---
frame_stato = tk.Frame(finestra, bd=1, relief=tk.SUNKEN)
frame_stato.pack(side=tk.BOTTOM, fill=tk.X)

label_stato = tk.Label(frame_stato, text="Pronto", anchor=tk.W)
label_stato.pack(side=tk.LEFT, fill=tk.X)

label_caratteri = tk.Label(frame_stato, text="Caratteri: 0", anchor=tk.E, padx=10)
label_caratteri.pack(side=tk.RIGHT)

# --- MENU ---
menu_bar = tk.Menu(finestra)

# Menu File
file_menu = tk.Menu(menu_bar, tearoff=0)
file_menu.add_command(label="Nuovo (Ctrl+N)", command=nuovo_file)
file_menu.add_command(label="Apri (Ctrl+O)", command=apri_file)
file_menu.add_command(label="Salva (Ctrl+S)", command=salva_file)
file_menu.add_separator()
file_menu.add_checkbutton(label="Salvataggio automatico", variable=auto_save_attivo)
file_menu.add_separator()
file_menu.add_command(label="Esci (Esc)", command=conferma_uscita)
menu_bar.add_cascade(label="File", menu=file_menu)

# Menu Modifica
edit_menu = tk.Menu(menu_bar, tearoff=0)
edit_menu.add_command(label="Taglia (Ctrl+X)", command=lambda: text_area.event_generate("<<Cut>>"))
edit_menu.add_command(label="Copia (Ctrl+C)", command=lambda: text_area.event_generate("<<Copy>>"))
edit_menu.add_command(label="Incolla (Ctrl+V)", command=lambda: text_area.event_generate("<<Paste>>"))
edit_menu.add_separator()
edit_menu.add_command(label="Trova e Sostituisci (F4)", command=apri_trova_sostituisci) # Nuova Voce Menu
edit_menu.add_separator()
edit_menu.add_command(label="Annulla (Ctrl+Z)", command=lambda: text_area.edit_undo())
edit_menu.add_command(label="Ripristina (Ctrl+Y)", command=lambda: text_area.edit_redo())
edit_menu.add_separator()
edit_menu.add_command(label="Impostazioni ⚙️", command=apri_impostazioni)
menu_bar.add_cascade(label="Modifica", menu=edit_menu)

# Menu Formato
format_menu = tk.Menu(menu_bar, tearoff=0)
format_menu.add_command(label="MAIUSCOLO", command=testo_maiuscolo)
format_menu.add_command(label="minuscolo", command=testo_minuscolo)
menu_bar.add_cascade(label="Formato", menu=format_menu)

# Menu Visualizza
view_menu = tk.Menu(menu_bar, tearoff=0)
view_menu.add_command(label="Tema Chiaro", command=lambda: text_area.config(bg="white", fg="black", insertbackground="black"))
view_menu.add_command(label="Tema Scuro", command=lambda: text_area.config(bg="#2c2c2c", fg="white", insertbackground="white"))
view_menu.add_separator()
view_menu.add_command(label="Zoom +", command=zoom_in)
view_menu.add_command(label="Zoom -", command=zoom_out)
view_menu.add_command(label="Zoom Reset", command=reset_zoom)
menu_bar.add_cascade(label="Visualizza", menu=view_menu)

# Menu Aiuto
help_menu = tk.Menu(menu_bar, tearoff=0)
help_menu.add_command(label="Guida all'uso", command=mostra_guida)
help_menu.add_command(label="Sito di Supporto", command=vai_al_sito)
help_menu.add_separator()
help_menu.add_command(label="Informazioni su blocchino", command=informazioni_software)
menu_bar.add_cascade(label="Aiuto", menu=help_menu)

finestra.config(menu=menu_bar)
finestra.protocol("WM_DELETE_WINDOW", conferma_uscita)

# Binding scorciatoie (Iniezione di F4 e Ctrl+F ridondante per la Alpha 1)
bindings = [("<Control-n>", nuovo_file), ("<Control-N>", nuovo_file), 
            ("<Control-o>", apri_file), ("<Control-O>", apri_file),
            ("<Control-s>", salva_file), ("<Control-S>", salva_file),
            ("<Escape>", conferma_uscita),
            ("<F4>", apri_trova_sostituisci), ("<Control-f>", apri_trova_sostituisci), ("<Control-F>", apri_trova_sostituisci),
            ("<Control-plus>", zoom_in), ("<Control-minus>", zoom_out), ("<Control-0>", reset_zoom),
            ("<Control-z>", lambda e: text_area.edit_undo()), ("<Control-Z>", lambda e: text_area.edit_undo()),
            ("<Control-y>", lambda e: text_area.edit_redo()), ("<Control-Y>", lambda e: text_area.edit_redo()),
            ("<Control-X>", lambda e: text_area.event_generate("<<Cut>>")),
            ("<Control-C>", lambda e: text_area.event_generate("<<Copy>>")),
            ("<Control-V>", lambda e: text_area.event_generate("<<Paste>>"))]

for key, func in bindings: 
    finestra.bind(key, func)

# --- ATTIVAZIONE CONTEGGIO E AGGIORNAMENTO AGGIUNTIVO ---
text_area.bind("<KeyRelease>", aggiorna_conteggio)

# --- AVVIO INTERFACCIA E CICLO AUTO-SAVE DIVINO ---
finestra.after(30000, auto_save_sentinel)
aggiorna_conteggio()  # Esegue un primo calcolo al boot
finestra.mainloop()