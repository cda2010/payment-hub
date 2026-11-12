(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ProduitDetailController', ProduitDetailController);

    ProduitDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Produit', 'Ecran'];

    function ProduitDetailController($scope, $rootScope, $stateParams, previousState, entity, Produit, Ecran) {
        var vm = this;

        vm.produit = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:produitUpdate', function(event, result) {
            vm.produit = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
