(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ProduitCanalDetailController', ProduitCanalDetailController);

    ProduitCanalDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ProduitCanal', 'Canal', 'Produit'];

    function ProduitCanalDetailController($scope, $rootScope, $stateParams, previousState, entity, ProduitCanal, Canal, Produit) {
        var vm = this;

        vm.produitCanal = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:produitCanalUpdate', function(event, result) {
            vm.produitCanal = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
