(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TableauFraisDetailController', TableauFraisDetailController);

    TableauFraisDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'TableauFrais', 'LigneFrais', 'TypeService', 'Canal', 'TypeUniteOrganisation', 'Client', 'UniteOrganisation'];

    function TableauFraisDetailController($scope, $rootScope, $stateParams, previousState, entity, TableauFrais, LigneFrais, TypeService, Canal, TypeUniteOrganisation, Client, UniteOrganisation) {
        var vm = this;

        vm.tableauFrais = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:tableauFraisUpdate', function(event, result) {
            vm.tableauFrais = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
