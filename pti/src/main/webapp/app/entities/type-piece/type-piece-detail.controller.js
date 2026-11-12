(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypePieceDetailController', TypePieceDetailController);

    TypePieceDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'TypePiece', 'Piece'];

    function TypePieceDetailController($scope, $rootScope, $stateParams, previousState, entity, TypePiece, Piece) {
        var vm = this;

        vm.typePiece = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:typePieceUpdate', function(event, result) {
            vm.typePiece = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
