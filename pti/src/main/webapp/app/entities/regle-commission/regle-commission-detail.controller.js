(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RegleCommissionDetailController', RegleCommissionDetailController);

    RegleCommissionDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'RegleCommission', 'TypeService', 'UniteOrganisation'];

    function RegleCommissionDetailController($scope, $rootScope, $stateParams, previousState, entity, RegleCommission, TypeService, UniteOrganisation) {
        var vm = this;

        vm.regleCommission = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:regleCommissionUpdate', function(event, result) {
            vm.regleCommission = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
