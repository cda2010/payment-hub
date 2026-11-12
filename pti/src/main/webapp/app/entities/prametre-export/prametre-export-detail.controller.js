(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PrametreExportDetailController', PrametreExportDetailController);

    PrametreExportDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'PrametreExport'];

    function PrametreExportDetailController($scope, $rootScope, $stateParams, previousState, entity, PrametreExport) {
        var vm = this;

        vm.prametreExport = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:prametreExportUpdate', function(event, result) {
            vm.prametreExport = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
