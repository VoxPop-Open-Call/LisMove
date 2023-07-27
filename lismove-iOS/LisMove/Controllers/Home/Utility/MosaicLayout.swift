/*
See LICENSE folder for this sample’s licensing information.

Abstract:
Custom view flow layout for mosaic-style appearance.
*/

import UIKit

public enum MosaicSegmentStyle {
    case fullWidth
    case fiftyFifty
    case twoThirdsOneThird
    case oneThirdTwoThirds
    case fullWidthCustomHeight(Float)
}

public protocol MosaicLayoutDelegate {
    
    func collectionView(_ collectionView:UICollectionView, mosaicCellSizeTypeAtIndexPath indexPath:IndexPath) -> MosaicSegmentStyle
    
}

public class MosaicLayout: UICollectionViewLayout {
    
    public var delegate:MosaicLayoutDelegate!
    
    public var items: [String] = ["dailyUse","user","msg","money","project","points","km","co2","carpooling","allarm","bike","device"]
    
    var contentBounds = CGRect.zero
    var cachedAttributes = [UICollectionViewLayoutAttributes]()
    
    /// - Tag: PrepareMosaicLayout
    public override func prepare() {
        super.prepare()
        
        guard let collectionView = collectionView else { return }

        // Reset cached information.
        cachedAttributes.removeAll()
        contentBounds = CGRect(origin: .zero, size: collectionView.bounds.size)
        
        // For every item in the collection view:
        //  - Prepare the attributes.
        //  - Store attributes in the cachedAttributes array.
        //  - Combine contentBounds with attributes.frame.
        let count = collectionView.numberOfItems(inSection: 0)
        
        var currentIndex = 0
        var segment: MosaicSegmentStyle = .fullWidth
        var lastFrame: CGRect = .zero
        
        let cvWidth = collectionView.bounds.size.width
        
        while currentIndex < count {
            var segmentFrame = CGRect(x: 0, y: lastFrame.maxY + 1.0, width: cvWidth, height: 150)
            var segmentRects = [CGRect]()
            
            let cellIndexPath = IndexPath(item: currentIndex, section: 0)
            let cellType = mosaicCellType(index: cellIndexPath)
            
            switch cellType {
            case .fullWidth:
                segmentFrame = CGRect(x: 0, y: lastFrame.maxY + 1.0, width: cvWidth, height: 150)
                
                segmentRects = [segmentFrame]
                
            case .fiftyFifty:
                segmentFrame = CGRect(x: 0, y: lastFrame.maxY + 1.0, width: cvWidth, height: 150)
                
                let horizontalSlices = segmentFrame.dividedIntegral(fraction: 0.5, from: .minXEdge)
                segmentRects = [horizontalSlices.first, horizontalSlices.second]
                
            case .twoThirdsOneThird:
                segmentFrame = CGRect(x: 0, y: lastFrame.maxY + 1.0, width: cvWidth, height: 180)
                
                let horizontalSlices = segmentFrame.dividedIntegral(fraction: (2.0 / 3.0), from: .minXEdge)
                let verticalSlices = horizontalSlices.second.dividedIntegral(fraction: 0.5, from: .minYEdge)
                segmentRects = [horizontalSlices.first, verticalSlices.first, verticalSlices.second]
                
            case .oneThirdTwoThirds:
                segmentFrame = CGRect(x: 0, y: lastFrame.maxY + 1.0, width: cvWidth, height: 180)
                
                let horizontalSlices = segmentFrame.dividedIntegral(fraction: (1.0 / 3.0), from: .minXEdge)
                let verticalSlices = horizontalSlices.first.dividedIntegral(fraction: 0.5, from: .minYEdge)
                segmentRects = [verticalSlices.first, verticalSlices.second, horizontalSlices.second]
                
            case .fullWidthCustomHeight(let height):
                let segmentFrame = CGRect(x: 0, y: lastFrame.maxY + 1.0, width: cvWidth, height: CGFloat(height))

                segmentRects = [segmentFrame]
            }
            
            // Create and cache layout attributes for calculated frames.
            for rect in segmentRects {
                let attributes = UICollectionViewLayoutAttributes(forCellWith: IndexPath(item: currentIndex, section: 0))

                let insetFrame = rect.insetBy(dx: 8, dy: 8)

                attributes.frame = insetFrame
                cachedAttributes.append(attributes)
                contentBounds = contentBounds.union(lastFrame)

                currentIndex += 1
                lastFrame = rect
            }
//
//            // Determine the next segment style.
//            switch currentIndex {
//            case 1:
//                segment = .twoThirdsOneThird
//            case 6:
//                segment = .fiftyFifty
//            case 9:
//                segment = .fiftyFifty
//            default:
//                segment = .fullWidth
//            }
        }
    }

    /// - Tag: CollectionViewContentSize
    public override var collectionViewContentSize: CGSize {
        return contentBounds.size
    }
    
    /// - Tag: ShouldInvalidateLayout
    public override func shouldInvalidateLayout(forBoundsChange newBounds: CGRect) -> Bool {
        guard let collectionView = collectionView else { return false }
        return !newBounds.size.equalTo(collectionView.bounds.size)
    }
    
    /// - Tag: LayoutAttributesForItem
    public override func layoutAttributesForItem(at indexPath: IndexPath) -> UICollectionViewLayoutAttributes? {
        return cachedAttributes[indexPath.item]
    }
    
    /// - Tag: LayoutAttributesForElements
    public override func layoutAttributesForElements(in rect: CGRect) -> [UICollectionViewLayoutAttributes]? {
        var attributesArray = [UICollectionViewLayoutAttributes]()
        
        // Find any cell that sits within the query rect.
        guard let lastIndex = cachedAttributes.indices.last,
              let firstMatchIndex = binSearch(rect, start: 0, end: lastIndex) else { return attributesArray }
        
        // Starting from the match, loop up and down through the array until all the attributes
        // have been added within the query rect.
        for attributes in cachedAttributes[..<firstMatchIndex].reversed() {
            guard attributes.frame.maxY >= rect.minY else { break }
            attributesArray.append(attributes)
        }
        
        for attributes in cachedAttributes[firstMatchIndex...] {
            guard attributes.frame.minY <= rect.maxY else { break }
            attributesArray.append(attributes)
        }
        
        return attributesArray
    }
    
    // Perform a binary search on the cached attributes array.
    func binSearch(_ rect: CGRect, start: Int, end: Int) -> Int? {
        if end < start { return nil }
        
        let mid = (start + end) / 2
        let attr = cachedAttributes[mid]
        
        if attr.frame.intersects(rect) {
            return mid
        } else {
            if attr.frame.maxY < rect.minY {
                return binSearch(rect, start: (mid + 1), end: end)
            } else {
                return binSearch(rect, start: start, end: (mid - 1))
            }
        }
    }

    
    
    // MARK: Delegate Wrappers
    
    func mosaicCellType(index indexPath:IndexPath) -> MosaicSegmentStyle {
        return delegate.collectionView(collectionView!, mosaicCellSizeTypeAtIndexPath:indexPath)
    }
    
    
}
extension CGRect {
    func dividedIntegral(fraction: CGFloat, from fromEdge: CGRectEdge) -> (first: CGRect, second: CGRect) {
        let dimension: CGFloat
        
        switch fromEdge {
        case .minXEdge, .maxXEdge:
            dimension = self.size.width
        case .minYEdge, .maxYEdge:
            dimension = self.size.height
        }
        
        let distance = (dimension * fraction).rounded(.up)
        var slices = self.divided(atDistance: distance, from: fromEdge)
        
        switch fromEdge {
        case .minXEdge, .maxXEdge:
            slices.remainder.origin.x += 1
            slices.remainder.size.width -= 1
        case .minYEdge, .maxYEdge:
            slices.remainder.origin.y += 1
            slices.remainder.size.height -= 1
        }
        
        return (first: slices.slice, second: slices.remainder)
    }
}
