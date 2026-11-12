(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('InfoSuppDetailController', InfoSuppDetailController);

    InfoSuppDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity'];

    function InfoSuppDetailController($scope, $rootScope, $stateParams, previousState, entity) {
        var vm = this;

        vm.infosupp = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:typePieceUpdate', function(event, result) {
            vm.infosupp = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
