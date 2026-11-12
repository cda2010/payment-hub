(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FactureDetailController', FactureDetailController);

    FactureDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Facture', 'Facturier'];

    function FactureDetailController($scope, $rootScope, $stateParams, previousState, entity, Facture, Facturier) {
        var vm = this;

        vm.facture = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:factureUpdate', function(event, result) {
            vm.facture = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
