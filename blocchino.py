import tkinter as tk
from tkinter import filedialog
from tkinter import messagebox
import webbrowser

# --- CONFIGURAZIONE ---
font_size = [12]

def nuovo_file():
    if text_area.get(1.0, "end-1c").strip():
        risposta = messagebox.askyesnocancel("Nuovo File", "Vuoi salvare le modifiche prima di creare un nuovo file?")
        if risposta is True:
            salva_file()
        elif risposta is None:
            return
    text_area.delete(1.0, tk.END)

def apri_file():
    file_path = filedialog.askopenfilename(filetypes=[("File di testo", "*.txt")])
    if file_path:
        with open(file_path, "r", encoding="utf-8") as file:
            contenuto = file.read()
            text_area.delete(1.0, tk.END)
            text_area.insert(tk.END, contenuto)

def salva_file():
    file_path = filedialog.asksaveasfilename(defaultextension=".txt",
                                             filetypes=[("File di testo", "*.txt")])
    if file_path:
        with open(file_path, "w", encoding="utf-8") as file:
            contenuto = text_area.get(1.0, tk.END)
            file.write(contenuto)
            messagebox.showinfo("Salvato", "File salvato con successo!")

def conferma_uscita():
    if text_area.get(1.0, "end-1c").strip():
        risposta = messagebox.askyesnocancel("Salva modifiche", "Hai del testo non salvato. Vuoi salvare prima di uscire?")
        if risposta is True:
            salva_file()
            finestra.destroy()
        elif risposta is False:
            finestra.destroy()
    else:
        finestra.destroy()

def mostra_guida():
    guida_testo = (
        "Comandi Rapidi di Blocchino:\n"
        "---------------------------\n"
        "• Ctrl+N : Nuovo Documento\n"
        "• Ctrl+O : Apri File esistente\n"
        "• Ctrl+S : Salva le tue modifiche\n"
        "• Ctrl+Plus/Minus : Regola lo Zoom\n"
        "• Ctrl+Z/Y : Annulla e Ripristina\n\n"
        "Usa il menu 'Visualizza' per cambiare il tema!"
    )
    messagebox.showinfo("Guida Rapida", guida_testo)

def informazioni_software():
    messagebox.showinfo("Informazioni", 
                        "Blocchino v2.0.0a2 ,Sviluppata Saffdds\n")
def zoom_in():
    font_size[0] += 2
    text_area.config(font=("Arial", font_size[0]))

def zoom_out():
    if font_size[0] > 6:
        font_size[0] -= 2
        text_area.config(font=("Arial", font_size[0]))

def reset_zoom():
    font_size[0] = 12
    text_area.config(font=("Arial", font_size[0]))
    
    # --- FUNZIONI DI FORMATO ---
def testo_maiuscolo():
    try:
        # Nota: start e end devono essere indentati di 4 spazi (o un Tab)
        start = text_area.index("sel.first")
        end = text_area.index("sel.last")
        selected_text = text_area.get(start, end)
        
        text_area.delete(start, end)
        text_area.insert(start, selected_text.upper())
    except tk.TclError:
        messagebox.showwarning("Formato", "Seleziona del testo per renderlo MAIUSCOLO!")

def testo_minuscolo():
    try:
        start = text_area.index("sel.first")
        end = text_area.index("sel.last")
        selected_text = text_area.get(start, end)
        
        text_area.delete(start, end)
        text_area.insert(start, selected_text.lower())
    except tk.TclError:
        messagebox.showwarning("Formato", "Seleziona del testo per renderlo minuscolo!")

def vai_al_sito():
    # Sostituisci l'URL con quello reale del tuo sito di Blocchino!
    webbrowser.open("https://saffdds.github.io/bellum_iustum/aiuto_blocchino.html")

# --- INTERFACCIA ---
finestra = tk.Tk()
finestra.title("Blocchino")
finestra.geometry("700x500")
finestra.minsize(400, 300) # Impedisce di rimpicciolire troppo il Blocchino
finestra.update_idletasks() # Forza l'aggiornamento dei widget

text_area = tk.Text(finestra, wrap="word", font=("Arial", font_size[0]), undo=True)
text_area.pack(expand=True, fill="both")

# --- MENU ---
menu_bar = tk.Menu(finestra)

# Menu File
file_menu = tk.Menu(menu_bar, tearoff=0)
file_menu.add_command(label="Nuovo (Ctrl+N)", command=nuovo_file)
file_menu.add_command(label="Apri (Ctrl+O)", command=apri_file)
file_menu.add_command(label="Salva (Ctrl+S)", command=salva_file)
file_menu.add_separator()
file_menu.add_command(label="Esci (Esc)", command=conferma_uscita)
menu_bar.add_cascade(label="File", menu=file_menu)

# Menu Modifica (RIPRISTINATO E COMPLETO)
edit_menu = tk.Menu(menu_bar, tearoff=0)
edit_menu.add_command(label="Taglia (Ctrl+X)", command=lambda: text_area.event_generate("<<Cut>>"))
edit_menu.add_command(label="Copia (Ctrl+C)", command=lambda: text_area.event_generate("<<Copy>>"))
edit_menu.add_command(label="Incolla (Ctrl+V)", command=lambda: text_area.event_generate("<<Paste>>"))
edit_menu.add_separator()
edit_menu.add_command(label="Annulla (Ctrl+Z)", command=lambda: text_area.edit_undo())
edit_menu.add_command(label="Ripristina (Ctrl+Y)", command=lambda: text_area.edit_redo())
menu_bar.add_cascade(label="Modifica", menu=edit_menu)

# --- MENU FORMATO ---
format_menu = tk.Menu(menu_bar, tearoff=0)
# Colleghiamo la voce "MAIUSCOLO" alla funzione che hai appena sistemato
format_menu.add_command(label="MAIUSCOLO", command=testo_maiuscolo)
# Colleghiamo la voce "minuscolo"
format_menu.add_command(label="minuscolo", command=testo_minuscolo)

# Aggiungiamo il menu "Formato" alla barra principale
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
help_menu.add_command(label="Pagina di supporto", command=vai_al_sito)
help_menu.add_separator()
help_menu.add_command(label="Informazioni su Blocchino", command=informazioni_software)
menu_bar.add_cascade(label="Aiuto", menu=help_menu)
finestra.config(menu=menu_bar)

# --- PROTOCOLLI E BINDING ---
finestra.protocol("WM_DELETE_WINDOW", conferma_uscita)

# Scorciatoie Universali (Fix Maiuscole/Minuscole)
bindings = [
    ("<Control-n>", nuovo_file), ("<Control-N>", nuovo_file),
    ("<Control-o>", apri_file), ("<Control-O>", apri_file),
    ("<Control-s>", salva_file), ("<Control-S>", salva_file),
    ("<Escape>", conferma_uscita),
    ("<Control-plus>", zoom_in), ("<Control-minus>", zoom_out), ("<Control-0>", reset_zoom),
    ("<Control-z>", text_area.edit_undo), ("<Control-Z>", text_area.edit_undo),
    ("<Control-y>", text_area.edit_redo), ("<Control-Y>", text_area.edit_redo),
    # Scorciatoie per Taglia/Copia/Incolla (Maiuscole)
    ("<Control-X>", lambda e: text_area.event_generate("<<Cut>>")),
    ("<Control-C>", lambda e: text_area.event_generate("<<Copy>>")),
    ("<Control-V>", lambda e: text_area.event_generate("<<Paste>>"))
]

for key, func in bindings:
    finestra.bind(key, lambda e, f=func: f() if callable(f) else f)

finestra.mainloop()