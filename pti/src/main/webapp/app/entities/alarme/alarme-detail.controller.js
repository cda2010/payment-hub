(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('AlarmeDetailController', AlarmeDetailController);

    AlarmeDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Alarme'];

    function AlarmeDetailController($scope, $rootScope, $stateParams, previousState, entity, Alarme) {
        var vm = this;

        vm.alarme = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:alarmeUpdate', function(event, result) {
            vm.alarme = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
