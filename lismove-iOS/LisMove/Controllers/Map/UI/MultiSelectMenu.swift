//
//  MultiMenuSheet.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 06/09/21.
//

import Foundation
import Sheeeeeeeeet


class MultiSelectMenu: OptionMenu {

    init(options: [MenuOption]) {
        let items = MultiSelectMenu.items(for: options)
        super.init(items: items)
    }
}

private extension MultiSelectMenu {
    
    static func items(for options: [MenuOption]) -> [MenuItem] {
        var items = [MenuItem]()
        items.append(contentsOf: itemsGroup(for: options, group: "Opzioni"))
        items.append(okButton)
        items.append(cancelButton)
        return items
    }
    
    static func itemsGroup(for options: [MenuOption], group: String) -> [MenuItem] {
        let items = options.map{MultiSelectItem(title: $0.name, isSelected: $0.isSelected)}
        return items
    }
}
