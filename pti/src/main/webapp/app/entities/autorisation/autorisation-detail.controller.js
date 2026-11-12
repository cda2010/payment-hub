(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('AutorisationDetailController', AutorisationDetailController);

    AutorisationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Autorisation', 'UniteOrganisation', 'TypeService'];

    function AutorisationDetailController($scope, $rootScope, $stateParams, previousState, entity, Autorisation, UniteOrganisation, TypeService) {
        var vm = this;

        vm.autorisation = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:autorisationUpdate', function(event, result) {
            vm.autorisation = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
