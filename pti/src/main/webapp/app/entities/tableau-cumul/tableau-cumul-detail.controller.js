(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TableauCumulDetailController', TableauCumulDetailController);

    TableauCumulDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'TableauCumul', 'TypeService', 'Canal', 'Client', 'UniteOrganisation'];

    function TableauCumulDetailController($scope, $rootScope, $stateParams, previousState, entity, TableauCumul, TypeService, Canal, Client, UniteOrganisation) {
        var vm = this;

        vm.tableauCumul = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:tableauCumulUpdate', function(event, result) {
            vm.tableauCumul = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
