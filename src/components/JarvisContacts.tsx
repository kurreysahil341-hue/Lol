/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from "react";
import { UserPlus, Trash2, Phone, Search, Users } from "lucide-react";
import { Contact } from "../types";

interface JarvisContactsProps {
  contacts: Contact[];
  onAddContact: (contact: Omit<Contact, "id">) => void;
  onDeleteContact: (id: string) => void;
}

export default function JarvisContacts({
  contacts,
  onAddContact,
  onDeleteContact,
}: JarvisContactsProps) {
  const [search, setSearch] = useState("");
  const [name, setName] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [relationship, setRelationship] = useState("");
  const [showAddForm, setShowAddForm] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !phoneNumber) return;
    
    onAddContact({
      name,
      phoneNumber,
      relationship: relationship || undefined,
    });
    
    // Reset Form
    setName("");
    setPhoneNumber("");
    setRelationship("");
    setShowAddForm(false);
  };

  const filteredContacts = contacts.filter(
    (c) =>
      c.name.toLowerCase().includes(search.toLowerCase()) ||
      c.phoneNumber.includes(search) ||
      (c.relationship && c.relationship.toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <div className="border border-cyan-500/20 bg-slate-950/70 p-6 rounded-2xl backdrop-blur-md relative overflow-hidden" id="jarvis-contacts-panel">
      {/* Visual framing accents */}
      <div className="absolute top-0 right-0 w-3 h-3 border-t border-r border-cyan-400/30" />
      <div className="absolute bottom-0 left-0 w-3 h-3 border-b border-l border-cyan-400/30" />

      <div className="flex items-center justify-between border-b border-cyan-500/10 pb-4 mb-4">
        <div className="flex items-center gap-2">
          <Users className="w-5 h-5 text-cyan-400" />
          <h2 className="text-sm font-bold tracking-widest text-cyan-200 uppercase font-mono">
            Jarvis Contacts Module
          </h2>
        </div>
        
        <button
          onClick={() => setShowAddForm(!showAddForm)}
          className="px-3 py-1 bg-cyan-950/50 hover:bg-cyan-900/60 border border-cyan-400/30 rounded text-[11px] font-mono tracking-wider text-cyan-300 flex items-center gap-1 transition-colors"
        >
          <UserPlus className="w-3.5 h-3.5" />
          {showAddForm ? "CLOSE FORM" : "ADD NEW"}
        </button>
      </div>

      <p className="text-[11px] text-cyan-400/60 font-mono mb-4 leading-relaxed">
        HINDI: Aap jis contact ko voice se call lagana chahte hain (jaise &quot;Call Papa&quot;), unhe yahan save karein. 
        Voice command trigger hote hi, Jarvis direct system dialer khol dega.
      </p>

      {/* Add New Contact Form */}
      {showAddForm && (
        <form onSubmit={handleSubmit} className="bg-slate-900/50 border border-cyan-500/10 rounded-lg p-4 mb-4 flex flex-col gap-3 font-mono text-xs">
          <h3 className="text-cyan-300 text-[11px] font-bold tracking-wider uppercase mb-1">Create System Contact</h3>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div className="flex flex-col gap-1">
              <label className="text-[10px] text-cyan-400/60 uppercase">Contact Name *</label>
              <input
                type="text"
                placeholder="e.g. Papa, Rahul, Mummy"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="bg-slate-950 border border-cyan-500/20 rounded px-2 py-1.5 text-cyan-100 outline-none focus:border-cyan-400"
                required
              />
            </div>
            
            <div className="flex flex-col gap-1">
              <label className="text-[10px] text-cyan-400/60 uppercase">Phone Number *</label>
              <input
                type="tel"
                placeholder="e.g. 9876543210"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
                className="bg-slate-950 border border-cyan-500/20 rounded px-2 py-1.5 text-cyan-100 outline-none focus:border-cyan-400"
                required
              />
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-[10px] text-cyan-400/60 uppercase">Relationship (Optional)</label>
              <input
                type="text"
                placeholder="e.g. Father, Friend, Mother"
                value={relationship}
                onChange={(e) => setRelationship(e.target.value)}
                className="bg-slate-950 border border-cyan-500/20 rounded px-2 py-1.5 text-cyan-100 outline-none focus:border-cyan-400"
              />
            </div>
          </div>

          <button
            type="submit"
            className="mt-2 w-full py-2 bg-cyan-600 hover:bg-cyan-500 text-slate-950 text-xs font-bold uppercase rounded transition-colors"
          >
            Authorize &amp; Link Contact
          </button>
        </form>
      )}

      {/* Search Input */}
      <div className="relative mb-4">
        <Search className="absolute left-3 top-2.5 w-4 h-4 text-cyan-400/40" />
        <input
          type="text"
          placeholder="Filter linked directories..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full bg-slate-950/80 border border-cyan-500/20 rounded-lg pl-9 pr-4 py-2 text-xs font-mono text-cyan-100 outline-none focus:border-cyan-400 placeholder:text-cyan-400/20"
        />
      </div>

      {/* Contacts List Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 max-h-64 overflow-y-auto pr-1 scrollbar-thin scrollbar-thumb-cyan-500/10">
        {filteredContacts.length === 0 ? (
          <div className="col-span-full py-8 text-center text-cyan-400/20 font-mono text-xs">
            [NO REGISTERED SYSTEM LINKS FOUND]
          </div>
        ) : (
          filteredContacts.map((contact) => (
            <div
              key={contact.id}
              className="border border-cyan-500/10 bg-slate-900/20 rounded-lg p-3 flex items-center justify-between hover:border-cyan-400/40 transition-all group"
            >
              <div className="flex flex-col gap-0.5 font-mono">
                <div className="flex items-center gap-1.5">
                  <span className="text-cyan-100 text-xs font-bold tracking-wide">
                    {contact.name}
                  </span>
                  {contact.relationship && (
                    <span className="px-1.5 py-0.5 bg-cyan-950 border border-cyan-400/20 rounded text-[9px] text-cyan-400 tracking-wide font-semibold uppercase">
                      {contact.relationship}
                    </span>
                  )}
                </div>
                <span className="text-cyan-400/60 text-[10px]">
                  {contact.phoneNumber}
                </span>
              </div>

              <div className="flex items-center gap-2">
                {/* Dial Trigger */}
                <a
                  href={`tel:${contact.phoneNumber}`}
                  className="p-1.5 bg-cyan-950/50 hover:bg-cyan-900/60 border border-cyan-400/20 rounded text-cyan-400 transition-colors"
                  title="Test Dialing Link"
                >
                  <Phone className="w-3.5 h-3.5" />
                </a>

                {/* Delete button */}
                <button
                  onClick={() => onDeleteContact(contact.id)}
                  className="p-1.5 bg-slate-950 hover:bg-red-950/30 border border-red-500/10 hover:border-red-500/30 rounded text-cyan-400/40 hover:text-red-400 transition-all opacity-40 group-hover:opacity-100"
                  title="Remove Link"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
