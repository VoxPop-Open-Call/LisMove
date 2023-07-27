//
//  VehicleOnBoardController.swift
//  LisMove
//
//  Created by Francesco Paolo Dellaquila on 11/08/21.
//

import SwiftUI
import SSSwiftUIGIFView
import ToastUI

struct VehicleOnBoardController: View {
    
    @State var step = 0
    @State private var searchText =  ""
    @State private var presentingToast: Bool = false
    @State private var lastStepPresentingToast: Bool = false
    @State var startTyping = false
    
    @ObservedObject var vehicleConfiguratorVM = VehicleConfiguratorVM()
    
    init() {
        /// These could be anywhere before the list has loaded.
        UITableView.appearance().backgroundColor = .white // tableview background
        UITableViewCell.appearance().backgroundColor = .white // cell background
    }

    
    
    var body: some View {
        
        ZStack {
            
            ScrollView {
                ZStack {
                    
                    
                    let announcingResult = Binding<Bool>(
                        get: { self.vehicleConfiguratorVM.success },
                        set: { _ in self.vehicleConfiguratorVM.success = false }
                    )
                    
                    Text("")
                                .alert(isPresented: announcingResult) {
                                    Alert(title: Text("Successo"),
                                        message: Text("Veicolo salvato con successo"),
                                        dismissButton: .default(Text("OK")){
                                            
                                            NotificationCenter.default.post(name: NSNotification.Name("dismissSwiftUI"), object: nil)
                                        })
                                }
                    
                    RoundedRectangle(cornerRadius: 25, style: .continuous)
                        .fill(Color.white)
                        .padding(.all, 16)

                    VStack {
                        
                        HStack {
                            Spacer()
                            
                            Button(action: {
                                
                                NotificationCenter.default.post(name: NSNotification.Name("dismissSwiftUI"), object: nil)
                                
                                
                            }, label: {
                                
                                Image(systemName: "x.circle.fill")
                                    .foregroundColor(.black)
                                    .font(.system(size: 26))
                                    .scaleEffect(x: 1.1)
                            })
                            .padding(.all, 8)
                        }
                        

                        switch step{
                        case 1:
                            
                            StepView(step: $step, startTyping: $startTyping, imageName: "car_1", gifCheck: false, text1: "Iniziamo dal Brand", text2: "Inizia a digitare il nome del brand", stepCheck: 1)

                            Group{
                                SearchBar(text: $searchText, placeholder: "Cerca Brand")
                                    .onTapGesture {
                                        startTyping = true
                                    }
                                
                                if startTyping  {
                                    List {
                                        ForEach(self.searchText.isEmpty ? vehicleConfiguratorVM.brand : vehicleConfiguratorVM.brand.filter{$0.name!.lowercased().contains(self.searchText.lowercased())}, id: \.self) { brand in
                                            HStack {
                                                Image("car_brand")
                                                    .resizable()
                                                    .frame(width: 56, height: 56)
                                    
                                                Text(brand.name!)
                                                    .onTapGesture {
                                                        //searchText = brand.name!
                                                        startTyping = false
                                                        step+=1
                                                        
                                                        self.vehicleConfiguratorVM.selectedBrand = brand
                                                        self.vehicleConfiguratorVM.syncCarModels(bid: brand.id!)
                                                    }
                                                    .foregroundColor(.black)
                                            }
                                        }
                                        .listRowBackground(Color.white)
                                    }
                                }
                            }
                            .padding(.leading, 16)
                            .padding(.top, 8)
                            .padding(.trailing, 16)
                            
                        case 2:
                            StepView(step: $step, startTyping: $startTyping, imageName: "car_2", gifCheck: false, text1: "Ora cerchiamo il modello", text2: "Ci aiuterà ad individuare tutti i dati necessari", stepCheck: 2)
                            
                            //searchbar with suggestions
                            Group {
                                SearchBar(text: $searchText, placeholder: "Cerca Modello")
                                    .onTapGesture {
                                        startTyping = true
                                    }
                                
                                if startTyping  {
                                    List {
                                        ForEach(vehicleConfiguratorVM.models.filter {
                                            self.searchText.isEmpty ? true : $0.name!.lowercased().contains(self.searchText.lowercased())
                                        }, id: \.self) { model in
                                            HStack {
                                                Image("car_brand")
                                                    .resizable()
                                                    .frame(width: 56, height: 56)
                                    
                                                Text(model.name!)
                                                    .onTapGesture {
                                                        //searchText = model.name!
                                                        startTyping = false
                                                        step+=1
                                                        
                                                        self.vehicleConfiguratorVM.selectedModel = model
                                                        self.vehicleConfiguratorVM.syncCarGenerations(bid: self.vehicleConfiguratorVM.selectedBrand!.id!, mid: model.id!)
                                                    }
                                                    .foregroundColor(.black)
                                            }
                                        }
                                        .listRowBackground(Color.white)
                                    }
                                }
                            }
                            .padding(.leading, 16)
                            .padding(.top, 8)
                            .padding(.trailing, 16)
                            
                        case 3:
                            StepView(step: $step, startTyping: $startTyping, imageName: "car_3", gifCheck: false, text1: "Infine seleziona il veicolo", text2: "Effettueremo una stima sulla base dei dati raccolti", stepCheck: 3)
                            
                            //searchbar with suggestions
                            Group {
                                SearchBar(text: $searchText, placeholder: "Cerca Veicolo")
                                    .onTapGesture {
                                        startTyping = true
                                    }
                                
                                if startTyping  {
                                    List {
                                        ForEach(vehicleConfiguratorVM.cars.filter {
                                            self.searchText.isEmpty ? true : $0.name!.lowercased().contains(self.searchText.lowercased())
                                        }, id: \.self) { car in
                                            HStack {
                                                Image("car_brand")
                                                    .resizable()
                                                    .frame(width: 56, height: 56)
                                    
                                                Text(car.name!)
                                                    .onTapGesture {
                                                        //searchText = car.name!
                                                        startTyping = false
                                                        step+=1
                                                        
                                                        self.vehicleConfiguratorVM.selectedCar = car
                                               
                                                    }
                                                    .foregroundColor(.black)
                                            }
                                     
                                        }
                                        .listRowBackground(Color.white)
                                    }
                                }
                            }
                            .padding(.leading, 16)
                            .padding(.top, 8)
                            .padding(.trailing, 16)
                            
                        case 4:
                            
                            Group {
                                VStack {
                                    
                                    HStack{
                                        Text("Ecco fatto")
                                            .bold()
                                            .font(.largeTitle)
                                            .padding(.leading, 16)
                                            .foregroundColor(.black)
                                        Spacer()
                                    }
                                    
                                    
                                    List {
                                        HStack {
                                            Image("car_brand")
                                                .resizable()
                                                .frame(width: 56, height: 56)
                                
                                            Text(self.vehicleConfiguratorVM.selectedBrand?.name ?? "")
                                                .foregroundColor(.black)
                                        }
                                        .listRowBackground(Color.white)
                                        
                                        HStack {
                                            Image("car_brand")
                                                .resizable()
                                                .frame(width: 56, height: 56)
                                
                                            Text(self.vehicleConfiguratorVM.selectedModel?.name ?? "")
                                                .foregroundColor(.black)
                                        }
                                        .listRowBackground(Color.white)
                                        
                                        HStack {
                                            Image("car_brand")
                                                .resizable()
                                                .frame(width: 56, height: 56)
                                
                                            Text(self.vehicleConfiguratorVM.selectedCar?.name ?? "")
                                                .foregroundColor(.black)
                                        }
                                        .listRowBackground(Color.white)
                                    }
                                    
                                    
                                }
                            }
                            
                            
                        default:
                            StepView(step: $step, startTyping: $startTyping, imageName: "co2", gifCheck: false, text1: "Aiuta a risparmiare C02", text2: "Seleziona tutti i dati del tue veicolo e \n stimeremo la c02 risparmiata", stepCheck: 0)
                        }
                

                        
                        Button(action: {
                            
                            if(step<4){
                                
                                if(step == 3){
                                    if(self.vehicleConfiguratorVM.selectedCar == nil){
                                        lastStepPresentingToast = true
                                    }else{
                                        step+=1
                                    }
                                }else{
                                    step+=1
                                }
                                

                                
                            }else if (step == 4){
                                
                                self.vehicleConfiguratorVM.saveUserCar()
                            }
                            
                        }) {
                            HStack {
                                Text(step == 0 ? "Inizia" : step == 4 ? "Conferma" : "Continua")
                                Image(systemName: "chevron.right")
                            }
                            .padding(.horizontal)
                            .padding()
                            .background(Capsule().fill(Color.red))
                            .accentColor(.white)
                            .animation(Animation.interpolatingSpring(stiffness: 50, damping: 10, initialVelocity: 10))
                        }
                        .animation(.spring(response: 0.4, dampingFraction: 0.5))
                        .padding(.all, 8)
                        .toast(isPresented: $lastStepPresentingToast) {
                          ToastView {
                            VStack {
                              Text("Completa prima tutti i passaggi")
                                .padding(.bottom)
                                .multilineTextAlignment(.center)

                              Button {
                                lastStepPresentingToast = false
                              } label: {
                                Text("OK")
                                  .bold()
                                  .foregroundColor(.white)
                                  .padding(.horizontal)
                                  .padding(.vertical, 12.0)
                                  .background(Color.accentColor)
                                  .cornerRadius(8.0)
                              }
                            }
                          }
                        }

                        
                        
                        Spacer()
                        
                        HStack(alignment: .center, spacing: 12) {
                            ForEach(0 ..< 4, id: \.self) {
                            
                                DotIndicator(pageIndex: $0, isOn: $step)
                                    .frame(width: 8, height: 8)
                                
                            }
                        }
                        .padding(.bottom,8)
                        
                        
                        
                    }.onTapGesture {
                        startTyping = false
                    }
                    .padding(.all, 24)
                
                    

                }
                .gesture(DragGesture(minimumDistance: 0, coordinateSpace: .local)
                                    .onEnded({ value in
                                        if value.translation.width < 0 {
                                            if(step < 4){
                                                if(step == 3){
                                                    if(self.vehicleConfiguratorVM.selectedCar == nil){
                                                        lastStepPresentingToast = true
                                                    }else{
                                                        step+=1
                                                    }
                                                }else{
                                                    step+=1
                                                }
                                            }

                                            startTyping = false
                                        }

                                        if value.translation.width > 0 {
                                            if(step > 0){
                                                // right
                                                step-=1
                                            }
                                            startTyping = false
                                        }
                                    })
                )
            
        }
  
        }
        .background(
            Image("wizard_bg")
                .resizable()
                .edgesIgnoringSafeArea(.all)
                .frame(width: UIScreen.main.bounds.width, height: UIScreen.main.bounds.height)
        )
        .toast(isPresented: $presentingToast) {
          ToastView {
            VStack {
              Text("Salvataggio in corso...")
                .padding(.bottom)
                .multilineTextAlignment(.center)

              Button {
                presentingToast = false
              } label: {
                Text("OK")
                  .bold()
                  .foregroundColor(.white)
                  .padding(.horizontal)
                  .padding(.vertical, 12.0)
                  .background(Color.accentColor)
                  .cornerRadius(8.0)
              }
            }
          }
        }

    }
}


struct VehicleOnBoardController_Previews: PreviewProvider {
    static var previews: some View {
        VehicleOnBoardController()
    }
}


extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (1, 1, 1, 0)
        }

        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue:  Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

struct StepView: View {
    
    @Binding var step: Int
    @Binding var startTyping: Bool
    
    //image name
    var imageName: String
    
    
    //gif check
    var gifCheck: Bool
    
    
    //text1
    var text1: String
    
    
    //text2
    var text2: String
    
    //step check
    var stepCheck: Int
    
    
    
    
    var body: some View {
        Group {
            
            if gifCheck {
            
                SwiftUIGIFPlayerView(gifName: imageName)
                    .frame(height: 250)
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(Color.gray, lineWidth: 0.5)
                    )
                    .padding(.all, 8)
                
            }else {
                
                Image(imageName)
                    .resizable()
                    .frame(height: startTyping ? 0 : 250)
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(Color.gray, lineWidth: 1)
                    )
                    .padding(.all, 8)
            }
                
            
            Text(text1)
                .font(.title)
                .foregroundColor(.black)
                .padding(.top, 16)
            
            
            Text(text2)
                .font(.subheadline)
                .bold()
                .padding(.top, 16)
                .foregroundColor(.black)
                .multilineTextAlignment(.center)
            
            

            
        }
        .opacity(step == stepCheck ? 1 : 0)
        .animation(.none)
        .scaleEffect(step == stepCheck ? 1 : 0.01)
        .animation(Animation.interpolatingSpring(stiffness: 50, damping: 10, initialVelocity: 5))
    }
}

struct SearchBar: UIViewRepresentable {

    @Binding var text: String
    var placeholder: String

    class Coordinator: NSObject, UISearchBarDelegate {

        @Binding var text: String

        init(text: Binding<String>) {
            _text = text
        }

        func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
            text = searchText
        }
    }

    func makeCoordinator() -> SearchBar.Coordinator {
        return Coordinator(text: $text)
    }

    func makeUIView(context: UIViewRepresentableContext<SearchBar>) -> UISearchBar {
        let searchBar = UISearchBar(frame: .zero)
        searchBar.delegate = context.coordinator
        searchBar.placeholder = placeholder
        searchBar.searchBarStyle = .minimal
        searchBar.autocapitalizationType = .none
        
        let textFieldInsideSearchBar = searchBar.value(forKey: "searchField") as? UITextField
        textFieldInsideSearchBar?.textColor = .black
        
        return searchBar
    }

    func updateUIView(_ uiView: UISearchBar, context: UIViewRepresentableContext<SearchBar>) {
        uiView.text = text
    }
}

struct DotIndicator: View {
    let pageIndex: Int

    @Binding var isOn: Int

    var body: some View {
        Button(action: {
            self.isOn = self.pageIndex
        }) {
            Image(systemName: "circle.fill")
                .imageScale(.small)
                .animation(.spring())
                .foregroundColor(isOn == pageIndex ? .black : .gray)
        }
    }
}
