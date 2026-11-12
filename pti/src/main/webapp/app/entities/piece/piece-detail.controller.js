(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PieceDetailController', PieceDetailController);

    PieceDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'DataUtils', 'entity', 'Piece', 'Personne', 'TypePiece'];

    function PieceDetailController($scope, $rootScope, $stateParams, previousState, DataUtils, entity, Piece, Personne, TypePiece) {
        var vm = this;

        vm.piece = entity;
        vm.previousState = previousState.name;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('ptiApp:pieceUpdate', function(event, result) {
            vm.piece = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
