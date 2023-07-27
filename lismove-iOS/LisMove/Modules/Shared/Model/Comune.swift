//
//  Comune.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 02/05/21.
//

import Foundation

// MARK: - Comune
class Comune: Codable{
    var nome, codice: String?
    var zona, regione, provincia: NestedData?
    var sigla, codiceCatastale: String?
    var cap: [String]?
    var popolazione: Int?

    init(nome: String?, codice: String?, zona: NestedData?, regione: NestedData?, provincia: NestedData?, sigla: String?, codiceCatastale: String?, cap: [String]?, popolazione: Int?) {
        self.nome = nome
        self.codice = codice
        self.zona = zona
        self.regione = regione
        self.provincia = provincia
        self.sigla = sigla
        self.codiceCatastale = codiceCatastale
        self.cap = cap
        self.popolazione = popolazione
    }
    
    enum CodingKeys: String, CodingKey {
        case nome = "nome"
        case codice = "codice"
        case zona = "zona"
        case regione = "regione"
        case provincia = "provincia"
        case sigla = "sigla"
        case codiceCatastale = "codiceCatastale"
        case cap = "cap"
        case popolazione = "popolazione"
    }
    
}

// MARK: - Provincia
class NestedData: Codable {
    var codice, nome: String?

    init(codice: String?, nome: String?) {
        self.codice = codice
        self.nome = nome
    }
    
    enum CodingKeys: String, CodingKey {
        case codice = "codice"
        case nome = "nome"
    }
}
