(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RoleValidationDetailController', RoleValidationDetailController);

    RoleValidationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'RoleValidation'];

    function RoleValidationDetailController($scope, $rootScope, $stateParams, previousState, entity, RoleValidation) {
        var vm = this;

        vm.roleValidation = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:roleValidationUpdate', function(event, result) {
            vm.roleValidation = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
