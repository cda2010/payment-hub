(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TemplateMessageDeleteController',TemplateMessageDeleteController);

    TemplateMessageDeleteController.$inject = ['$uibModalInstance', 'entity', 'TemplateMessage'];

    function TemplateMessageDeleteController($uibModalInstance, entity, TemplateMessage) {
        var vm = this;

        vm.templateMessage = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            TemplateMessage.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
