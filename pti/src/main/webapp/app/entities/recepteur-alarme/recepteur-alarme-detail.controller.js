(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RecepteurAlarmeDetailController', RecepteurAlarmeDetailController);

    RecepteurAlarmeDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'RecepteurAlarme'];

    function RecepteurAlarmeDetailController($scope, $rootScope, $stateParams, previousState, entity, RecepteurAlarme) {
        var vm = this;

        vm.recepteurAlarme = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:recepteurAlarmeUpdate', function(event, result) {
            vm.recepteurAlarme = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
