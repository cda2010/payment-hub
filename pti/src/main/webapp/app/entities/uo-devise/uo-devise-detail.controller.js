(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UoDeviseDetailController', UoDeviseDetailController);

    UoDeviseDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'UoDevise', 'Devise', 'UniteOrganisation'];

    function UoDeviseDetailController($scope, $rootScope, $stateParams, previousState, entity, UoDevise, Devise, UniteOrganisation) {
        var vm = this;

        vm.uoDevise = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:uoDeviseUpdate', function(event, result) {
            vm.uoDevise = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
