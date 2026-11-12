(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FournisseurServiceDetailController', FournisseurServiceDetailController);

    FournisseurServiceDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'FournisseurService', 'UniteOrganisation', 'Pays'];

    function FournisseurServiceDetailController($scope, $rootScope, $stateParams, previousState, entity, FournisseurService, UniteOrganisation, Pays) {
        var vm = this;

        vm.fournisseurService = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:fournisseurServiceUpdate', function(event, result) {
            vm.fournisseurService = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
